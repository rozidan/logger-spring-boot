/**
 * Copyright (C) 2019 Idan Roz the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rozidan.springboot.logger;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

/**
 * AspectJ to intercept {@link Loggable} methods or classes.
 *
 * @author Idan Rozenfeld
 */
@Aspect
@Component
public class LoggerInterceptor {

    private Logger logger;

    private LoggerMsgArgsGenerator lmag;

    private Set<WarnPoint> warnPoints;
    private ScheduledExecutorService warnService;

    @Autowired
    public LoggerInterceptor(Logger logger) {
        this.lmag = new LoggerMsgArgsGenerator();
        this.logger = logger;
    }

    @PostConstruct
    protected void construct() {
        warnPoints = new ConcurrentSkipListSet<>();
        warnService = Executors.newSingleThreadScheduledExecutor();
        warnService.scheduleAtFixedRate(() -> {
            for (WarnPoint wp : warnPoints) {
                long duration = System.nanoTime() - wp.getStart();
                if (isOver(duration, wp.getLoggable())) {
                    log(LogLevel.WARN, "#{}({}): in {} and still running (max {})",
                            wp.getPoint(), wp.getLoggable(),
                            lmag.warnBefore(wp.getPoint(), wp.getLoggable(), duration));
                    warnPoints.remove(wp);
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    @Pointcut("execution(public * *(..))"
            + " && !execution(String *.toString())"
            + " && !execution(int *.hashCode())"
            + " && !execution(boolean *.canEqual(Object))"
            + " && !execution(boolean *.equals(Object))")
    protected void publicMethod() {
    }

    @Pointcut("@annotation(loggable)")
    protected void loggableMethod(Loggable loggable) {
    }

    @Pointcut("@within(loggable)")
    protected void loggableClass(Loggable loggable) {
    }

    @Around(value = "publicMethod() && loggableMethod(loggable)", argNames = "joinPoint,loggable")
    public Object logExecutionMethod(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        return logMethod(joinPoint, loggable);
    }

    @Around(value = "publicMethod() && loggableClass(loggable) && !loggableMethod(com.github.rozidan.springboot.logger.Loggable)", argNames = "joinPoint,loggable")
    public Object logExecutionClass(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        return logMethod(joinPoint, loggable);
    }

    public Object logMethod(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        long start = System.nanoTime();
        WarnPoint warnPoint = null;
        Object returnVal;

        if (isLevelEnabled(joinPoint, loggable) && loggable.warnOver() >= 0) {
            warnPoint = new WarnPoint(joinPoint, loggable, start);
            warnPoints.add(warnPoint);
        }

        if (loggable.entered()) {
            log(loggable.value(), "#{}({}): entered", joinPoint,
                    loggable, lmag.enter(joinPoint, loggable));
        }

        try {
            returnVal = joinPoint.proceed();

            long nano = System.nanoTime() - start;
            if (isOver(nano, loggable)) {
                log(LogLevel.WARN, "#{}({}): {} in {} (max {})",
                        joinPoint, loggable, lmag.warnAfter(joinPoint, loggable, returnVal, nano));
            } else {
                log(loggable.value(), "#{}({}): {} in {}", joinPoint, loggable,
                        lmag.after(joinPoint, loggable, returnVal, nano));
            }
            return returnVal;
        } catch (Throwable ex) {
            if (contains(loggable.ignore(), ex)) {
                log(LogLevel.ERROR, "#{}({}): thrown {}({}) from {}[{}] in {}",
                        joinPoint, loggable, lmag.error(joinPoint, loggable, System.nanoTime() - start, ex));
            } else {
                log(LogLevel.ERROR, "#{}({}): thrown {}({}) from {}[{}] in {}",
                        joinPoint, loggable, lmag.errorWithException(joinPoint, loggable, System.nanoTime() - start, ex));
            }
            throw ex;
        } finally {
            if (warnPoint != null) {
                warnPoints.remove(warnPoint);
            }
        }
    }

    private void log(LogLevel level, String message, ProceedingJoinPoint joinPoint, Loggable loggable, Object... args) {
        if (loggable.name().isEmpty()) {
            logger.log(level, ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaringClass(), message, args);
        } else {
            logger.log(level, loggable.name(), message, args);
        }
    }

    private boolean isLevelEnabled(ProceedingJoinPoint joinPoint, Loggable loggable) {
        return loggable.name().isEmpty()
                ? logger.isEnabled(LogLevel.WARN,
                ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaringClass())
                : logger.isEnabled(LogLevel.WARN, loggable.name());
    }

    private boolean isOver(long nano, Loggable loggable) {
        return loggable.warnOver() >= 0
                && TimeUnit.NANOSECONDS.toMillis(nano) > loggable.warnUnit().toMillis(loggable.warnOver());
    }

    private boolean contains(Class<? extends Throwable>[] array, Throwable exp) {
        boolean contains = false;
        for (final Class<? extends Throwable> type : array) {
            if (instanceOf(exp.getClass(), type)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private boolean instanceOf(Class<?> child, Class<?> parent) {
        boolean instance = child.equals(parent)
                || child.getSuperclass() != null && instanceOf(child.getSuperclass(), parent);
        if (!instance) {
            for (final Class<?> iface : child.getInterfaces()) {
                instance = instanceOf(iface, parent);
                if (instance) {
                    break;
                }
            }
        }
        return instance;
    }

    @EqualsAndHashCode(of = "point")
    @AllArgsConstructor
    @Getter
    protected static class WarnPoint implements Comparable<WarnPoint> {

        private ProceedingJoinPoint point;
        private Loggable loggable;
        private long start;

        @Override
        public int compareTo(WarnPoint obj) {
            return Long.compare(obj.getStart(), start);
        }
    }

}
