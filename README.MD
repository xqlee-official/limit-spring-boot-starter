# spring boot Single project traffic limiting framework


## Usage
### Configuration description
maven pom deps
````xml
        <dependency>
            <groupId>com.xqlee.boot.limiter</groupId>
            <artifactId>limiter-spring-boot-starter</artifactId>
            <version>latest</version>
        </dependency>
````
`application.yml` Add the following configuration (The default global rate limit is 50)
````yaml
spring:
  limiter:
    global-qps: 50 #global default value 50 
    special-mapping: {'[/admin/**]':5,'[/web/**]':6} #Local setting
    ignore-urls: #Ignore urls，Support /** matching
      - /assets/**
      - /assets/**.css #Ignore all css files in the /assets/ directory
      - /**.jpg #Ignore all jpg images
      - /404.html
      - /403.html
    ignore-uas: #Ignored UA containing some special characters
      - spider
    ignore-priority: true # If ignore conflicts with the special traffic limit of special-mapping, it is ignored by default. false- Goes to the special current limit
````
- 1.Global current limiting is set. If the number is less than or equal to 0, the fully limited current converter is not initialized.
- 2.Local traffic limiting configuration, Map structure, and special characters such as/in key must be wrapped in brackets. Support /**, for example: '[/api/s1/**]'
- 3.Ignore the configuration, especially if it is a web project, you must ignore the resource file and the error page file, see the example above. Support /** writing.
### Limiting Exception
Traffic limiting exceptions. There are three traffic limiting exceptions
- `LimiterException` The traffic limiting parent is not directly received
- `GlobalLimiterException` extend `LimiterException`The global traffic limiting exception
- `SpecialLimiterException`extend `LimiterException`The local traffic limiting exception

You can use Spring's `@ControllerAdvice` annotation class or `@RestControllerAdvice` annotation class to intercept exceptions and handle the actions after the exception

### Change logs

#### 1.0.0
- Initial release
- Support for springboot single project
- Support global traffic limiting
- Support limiting by specified address
- `**` matching is supported