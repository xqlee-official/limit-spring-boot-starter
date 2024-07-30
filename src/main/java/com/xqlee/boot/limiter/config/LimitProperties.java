/*
 * Copyright 2017-2024 xqlee.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xqlee.boot.limiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConfigurationProperties(prefix = "spring.limiter")
@Data
public class LimitProperties {
    /**
     * 是否启用限流框架，默认启用
     */
    boolean enabled=true;
    /***
     * 全局限速每秒通过请求数量，默认50，小于等于0，则不做限速
     */
    double globalQps=50;

    /***
     * 局部指定URL限流
     * key- 限流url
     * value - 限流频率 qps
     */
    Map<String,Double> specialMapping=new LinkedHashMap<>();

    /***
     * 忽略限制的url
     */
    List<String> ignoreUrls=new ArrayList<>();
    /**
     * 忽略的UA，包含则忽略
     */
    List<String> ignoreUas=new ArrayList<>();
    /**
     * 忽略优先级高（当忽略与特殊冲突，true-走忽略流程;false-走特殊限流）
     */
    boolean ignorePriority=true;

}
