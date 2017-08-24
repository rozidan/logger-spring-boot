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

import lombok.*;

/**
 * <p>
 * Logger message formats that can be replaced with the defaults.
 * </p>
 * There are list of placeholder's that can be used:
 * <ul>
 * <li>${method.name} - method name</li>
 * <li>${method.args} - method arguments</li>
 * <li>${method.result} - method results</li>
 * <li>${method.duration} - method runtime duration</li>
 * <li>${method.warn.duration} - method runtime warning duration to be displayed</li>
 * <li>${error.class.name} - exception class name</li>
 * <li>${error.message} - exception message</li>
 * <li>${error.source.class.name} - exception source class name</li>
 * <li>${error.source.line} - exception source line number that cause the exception</li>
 * </ul>
 *
 * @author Idan Rozenfeld
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoggerFormats {

    private String enter;
    private String warnBefore;
    private String warnAfter;
    private String after;
    private String error;

}
