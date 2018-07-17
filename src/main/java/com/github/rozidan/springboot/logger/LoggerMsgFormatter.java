/**
 * Copyright (C) 2018 Idan Rozenfeld the original author or authors
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

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Helper class for log message format.
 *
 * @author Idan
 */
final class LoggerMsgFormatter {

    private static final String DOTS = "..";

    private static final String METHDO_VALUE = "method.name";
    private static final String ARGS_VALUE = "method.args";
    private static final String RESULT_VALUE = "method.result";
    private static final String DURATION_VALUE = "method.duration";
    private static final String WARN_DURATION_VALUE = "method.warn.duration";
    private static final String ERROR_CLASS_VALUE = "error.class.name";
    private static final String ERROR_MSG_VALUE = "error.message";
    private static final String ERROR_SOURCE_CLASS_VALUE = "error.source.class.name";
    private static final String ERROR_LINE_VALUE = "error.source.line";

    private LoggerFormats formats;

    public LoggerMsgFormatter(LoggerFormats formats) {
        this.formats = formats;
    }

    public String enter(ProceedingJoinPoint joinPoint, Loggable loggable) {
        Map<String, Object> values = new HashMap<>();
        values.put(METHDO_VALUE, methodName(joinPoint));
        values.put(ARGS_VALUE, methodArgs(joinPoint, loggable));
        return StrSubstitutor.replace(formats.getEnter(), values);
    }

    public String warnBefore(ProceedingJoinPoint joinPoint, Loggable loggable, long nano) {
        Map<String, Object> values = new HashMap<>();
        values.put(METHDO_VALUE, methodName(joinPoint));
        values.put(ARGS_VALUE, methodArgs(joinPoint, loggable));
        values.put(DURATION_VALUE, durationString(nano));
        values.put(WARN_DURATION_VALUE, warnDuration(loggable));
        return StrSubstitutor.replace(formats.getWarnBefore(), values);
    }

    public String warnAfter(ProceedingJoinPoint joinPoint, Loggable loggable, Object result, long nano) {
        Map<String, Object> values = new HashMap<>();
        values.put(METHDO_VALUE, methodName(joinPoint));
        values.put(ARGS_VALUE, methodArgs(joinPoint, loggable));
        values.put(DURATION_VALUE, durationString(nano));
        values.put(WARN_DURATION_VALUE, warnDuration(loggable));
        values.put(RESULT_VALUE, methodResults(result, loggable));
        return StrSubstitutor.replace(formats.getWarnAfter(), values);
    }

    public String after(ProceedingJoinPoint joinPoint, Loggable loggable, Object result, long nano) {
        Map<String, Object> values = new HashMap<>();
        values.put(METHDO_VALUE, methodName(joinPoint));
        values.put(ARGS_VALUE, methodArgs(joinPoint, loggable));
        values.put(DURATION_VALUE, durationString(nano));
        values.put(RESULT_VALUE, methodResults(result, loggable));
        return StrSubstitutor.replace(formats.getAfter(), values);
    }

    public String error(ProceedingJoinPoint joinPoint, Loggable loggable, long nano, Throwable err) {
        Map<String, Object> values = new HashMap<>();
        values.put(METHDO_VALUE, methodName(joinPoint));
        values.put(ARGS_VALUE, methodArgs(joinPoint, loggable));
        values.put(DURATION_VALUE, durationString(nano));
        values.put(ERROR_CLASS_VALUE, errClass(err));
        values.put(ERROR_MSG_VALUE, errMsg(err));
        values.put(ERROR_SOURCE_CLASS_VALUE, errSourceClass(err));
        values.put(ERROR_LINE_VALUE, errLine(err));
        return StrSubstitutor.replace(formats.getError(), values);
    }

    private String warnDuration(Loggable loggable) {
        return Duration.ofMillis(loggable.warnUnit().toMillis(loggable.warnOver())).toString();
    }

    private String methodName(JoinPoint joinPoint) {
        return MethodSignature.class.cast(joinPoint.getSignature()).getMethod().getName();
    }

    private String methodArgs(JoinPoint joinPoint, Loggable loggable) {
        return loggable.skipArgs() ? DOTS : argsToString(joinPoint.getArgs());
    }

    private String methodResults(Object result, Loggable loggable) {
        return loggable.skipResult() ? DOTS : argsToString(result);
    }

    private String errClass(Throwable err) {
        return err.getClass().getName();
    }

    private String errMsg(Throwable err) {
        return err.getMessage();
    }

    private int errLine(Throwable err) {
        if (err.getStackTrace().length > 0) {
            return err.getStackTrace()[0].getLineNumber();
        }
        return -1;
    }

    private String errSourceClass(Throwable err) {
        if (err.getStackTrace().length > 0) {
            return err.getStackTrace()[0].getClassName();
        }
        return "somewhere";
    }

    private String durationString(long nano) {
        return Duration.ofMillis(TimeUnit.NANOSECONDS.toMillis(nano)).toString();
    }

    private String argsToString(Object arg) {
        String text;
        if (arg == null) {
            return "NULL";
        } else if (arg.getClass().isArray()) {
            if (arg instanceof Object[]) {
                text = objectArraysToString((Object[]) arg);
            } else {
                text = primitiveArrayToString(arg);
            }
        } else {
            String origin = arg.toString();
            if (arg instanceof String || origin.isEmpty()) {
                text = String.format("'%s'", origin);
            } else {
                text = origin;
            }
        }
        return text;
    }

    private String objectArraysToString(Object... arg) {
        StringBuilder bldr = new StringBuilder();
        bldr.append('[');
        for (Object item : arg) {
            if (bldr.length() > 1) {
                bldr.append(",").append(" ");
            }
            bldr.append(argsToString(item));
        }
        return bldr.append(']').toString();
    }

    private String primitiveArrayToString(Object arg) {
        String text;
        if (arg instanceof char[]) {
            text = Arrays.toString((char[]) arg);
        } else if (arg instanceof byte[]) {
            text = Arrays.toString((byte[]) arg);
        } else if (arg instanceof short[]) {
            text = Arrays.toString((short[]) arg);
        } else if (arg instanceof int[]) {
            text = Arrays.toString((int[]) arg);
        } else if (arg instanceof long[]) {
            text = Arrays.toString((long[]) arg);
        } else if (arg instanceof float[]) {
            text = Arrays.toString((float[]) arg);
        } else if (arg instanceof double[]) {
            text = Arrays.toString((double[]) arg);
        } else if (arg instanceof boolean[]) {
            text = Arrays.toString((boolean[]) arg);
        } else {
            text = "[unknown]";
        }
        return text;
    }
}
