# Spring Boot Logger
> A Spring Boot `@Loggable` annotation that will help you log your application more easily.

> Base on [jcabi-aspects](http://aspects.jcabi.com/annotation-loggable.html) project `@Loggable` annotation.

[![Build Status](https://travis-ci.org/rozidan/logger-spring-boot.svg?branch=master)](https://travis-ci.org/rozidan/logger-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/rozidan/logger-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/rozidan/logger-spring-boot?branch=master)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Features
Register the Logger to your Spring Boot application and allow it to log wherever you tell it to.
The Logger uses the slf4j to support abstraction for various logging frameworks.

## Setup

In order to add logger to your project simply add this dependency to your classpath:

```xml
<dependency>
    <groupId>com.github.rozidan</groupId>
    <artifactId>logger-spring-boot</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```groovy
compile 'com.github.rozidan:logger-spring-boot:1.0.0-SNAPSHOT'
```

## Log your application

Apply the Logger to your application with `@EnableLogger` annotation in a configuration class:

```java
@Configuration
@EnableLogger
public class LoggerConfig {

}
```

Simply add the `@Loggable` annotation to a method, or to a class scope:

```java
@RestController
@RequestMapping(path = "/employees")
public class EmployeeController {
	
	@Loggable
	@GetMapping
	public List<EmployeeDto> listAllEmployees() {
	}
}
```
More examples:

Warning whenever execution is over 2 sec:
```java
@Loggable(warnOver = 2, warnUnit = TimeUnit.SECONDS)
```
This will result 2 lines of log, one where 2 sec are over, and the other when execution is complete:
```text
.....c.i.s.l.w.c.EmployeeController           : #listAllEmployees([]): in PT2.833S and still running (max PT0.002S)
.....c.i.s.l.w.c.EmployeeController           : #listAllEmployees([]): [] in PT6.345S (max PT0.002S)
```

Log when enter to a method:
```java
@Loggable(entered = true)
```

Skip printing arguments and results of a method:
```java
@Loggable(skipArgs = true, skipResult = true)
```

Log with different level (default is INFO):
```java
@Loggable(LogLevel.WARN)
```

Set a different logger name (default is class name):
```java
@Loggable(value = LogLevel.WARN, name = "my-logger-name")
```

## Log messages custom format

To change the log messages format simply create bean of type `LoggerFormats`

```java
@Configuration
@EnableLogger
public class LoggerConfig {
	@Bean
	public LoggerFormats loggerFormats() {
		return LoggerFormats.builder()
			.before("...")
			.after("...)
			...
			.build();
	}
}
```
Please see `loggerFormats` javadoc for available placeholders.



## License

[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)
