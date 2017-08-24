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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("override-formats")
public class LoggableOverride2FormatsTest {

    @Rule
    public OutputCapture capture = new OutputCapture();

    @Autowired
    private SomeService3 someService3;

    @BeforeClass
    public static void setErrorLogging() {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
    }

    @Test
    public void overrideFormats() {
        someService3.defaultLog();
        assertThat(capture.toString(), containsString(
                "INFO com.github.rozidan.springboot.logger.LoggableOverride2FormatsTest$SomeService3 - "
                        + "override format defaultLog"));
    }

    public static class SomeService3 {

        @Loggable
        public void defaultLog() {

        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableLogger
    public static class Application {
        @Bean
        public SomeService3 someService3() {
            return new SomeService3();
        }

        @Bean
        @Profile("override-formats")
        public LoggerFormats loggerFormats() {
            LoggerFormats format = new LoggerFormats();
            format.setEnter("");
            format.setAfter("override format ${method.name}");
            format.setWarnBefore("");
            format.setWarnAfter("");
            format.setError("");

            return format;
        }
    }

}