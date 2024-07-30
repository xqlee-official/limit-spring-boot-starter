# spring boot 单项目限流框架


## 使用说明 Usage
### 配置说明
首先在maven配置文件pom.xml添加依赖
````xml
        <dependency>
            <groupId>com.xqlee.boot</groupId>
            <artifactId>limiter-spring-boot-starter</artifactId>
            <version>最新版本</version>
        </dependency>
````
application.yml添加下面配置，默认情况启用限流，全局50QPS
````yaml
spring:
  limiter:
    global-qps: 50 #全局限流，默认50
    special-mapping: {'[/admin/**]':5,'[/web/**]':6} #局部限流配置
    ignore-urls: #忽略url，支持/**写法
      - /assets/**
      - /assets/**.css #忽略 /assets/目录下所有css文件
      - /**.jpg #忽略所有jpg图片
      - /404.html
      - /403.html
    ignore-uas: #忽略的包含某些特殊字符的UA
      - spider
    ignore-priority: true # 忽略与special-mapping特殊限流冲突时候，默认true-走忽略；false-走特殊限流
````
- 1.全局设置限流，数字小于等于0则不初始化全局限流器；
- 2.局部限流配置，Map结构，注意key中有/等特殊字符需要用中括号包裹起来。支持/**写法，例如：'[/api/s1/**]'
- 3.忽略配置，如果是web项目尤其重要，必须忽略资源文件还有错误页面文件，参考上方例子。支持/**写法。
### 限流异常说明
限流异常，限流异常有三个，分别是
- `LimiterException`限流父异常不会直接收到
- `GlobalLimiterException` 继承于`LimiterException`全局限流异常
- `SpecialLimiterException`继承于`LimiterException`局部限流异常

可以通过Spring 的 `@ControllerAdvice`注解类或者`@RestControllerAdvice`注解类进行异常拦截并处理异常情况后的操作

### 日志

#### 1.0.0
- 初始版本
- 支持spring boot 单一项目
- 支持全局限流配置
- 支持指定地址特殊限流配置
- 支持`**`匹配