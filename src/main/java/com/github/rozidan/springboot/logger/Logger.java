/**
 * Copyright (C) 2017 Idan Rozenfeld the original author or authors
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

import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

/**
 * Helper method for logger.
 *
 * @author Idan Rozenfeld
 *
 */
@Component
public final class Logger {

   private void log(org.slf4j.Logger logger, LogLevel level, String message) {
      switch (level) {
         case TRACE:
            logger.trace(message);
            break;
         case DEBUG:
            logger.debug(message);
            break;
         case INFO:
            logger.info(message);
            break;
         case WARN:
            logger.warn(message);
            break;
         case ERROR:
         case FATAL:
            logger.error(message);
            break;
         default:
            break;
      }
   }

   private void log(org.slf4j.Logger logger, LogLevel level, String message, Throwable err) {
      switch (level) {
         case TRACE:
            logger.trace(message, err);
            break;
         case DEBUG:
            logger.debug(message, err);
            break;
         case INFO:
            logger.info(message, err);
            break;
         case WARN:
            logger.warn(message, err);
            break;
         case ERROR:
         case FATAL:
            logger.error(message, err);
            break;
         default:
            break;
      }
   }

   public void log(LogLevel level, Class<?> clazz, String message) {
      log(LoggerFactory.getLogger(clazz), level, message);
   }

   public void log(LogLevel level, String name, String message) {
      log(LoggerFactory.getLogger(name), level, message);
   }

   public void log(Class<?> clazz, String message, Throwable err) {
      log(LoggerFactory.getLogger(clazz), LogLevel.ERROR, message, err);
   }

   public void log(LogLevel level, String name, String message, Throwable err) {
      log(LoggerFactory.getLogger(name), level, message, err);
   }

   private boolean isEnabled(org.slf4j.Logger logger, LogLevel level) {
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
            return false;
      }
   }

   public boolean isEnabled(LogLevel level, Class<?> clazz) {
      return isEnabled(LoggerFactory.getLogger(clazz), level);
   }

   public boolean isEnabled(LogLevel level, String name) {
      return isEnabled(LoggerFactory.getLogger(name), level);
   }
}
