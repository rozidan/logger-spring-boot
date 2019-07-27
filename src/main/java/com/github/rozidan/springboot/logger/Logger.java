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

import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

/**
 * Helper method for logger.
 *
 * @author Idan Rozenfeld
 */
@Component
public final class Logger {

    public void log(LogLevel level, Class<?> clazz, String message, Object... args) {
        log(LoggerFactory.getLogger(clazz), level, message, args);
    }

    public void log(LogLevel level, String name, String message, Object... args) {
        log(LoggerFactory.getLogger(name), level, message, args);
    }

    public boolean isEnabled(LogLevel level, Class<?> clazz) {
        return isEnabled(LoggerFactory.getLogger(clazz), level);
    }

    public boolean isEnabled(LogLevel level, String name) {
        return isEnabled(LoggerFactory.getLogger(name), level);
    }

    private void log(org.slf4j.Logger logger, LogLevel level, String message, Object... args) {
        Objects.requireNonNull(level, "LogLevel must not be null.");
        switch (level) {
            case TRACE:
                logger.trace(message, args);
                break;
            case DEBUG:
                logger.debug(message, args);
                break;
            case INFO:
                logger.info(message, args);
                break;
            case WARN:
                logger.warn(message, args);
                break;
            case ERROR:
            case FATAL:
                logger.error(message, args);
                break;
            default:
                break;
        }
    }

    private boolean isEnabled(org.slf4j.Logger logger, LogLevel level) {
        Objects.requireNonNull(level, "LogLevel must not be null.");
        switch (level) {
            case TRACE:
                return logger.isTraceEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case ERROR:
            case FATAL:
                return logger.isErrorEnabled();
            default:
                throw new IllegalArgumentException("LogLevel must be one of the enabled levels.");
        }
    }

}
