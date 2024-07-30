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

package com.xqlee.boot.limiter.interceptor;

import com.google.common.util.concurrent.RateLimiter;
import com.xqlee.boot.limiter.exception.GlobalLimiterException;
import lombok.extern.slf4j.Slf4j;
import com.xqlee.boot.limiter.config.LimitProperties;
import com.xqlee.boot.limiter.exception.SpecialLimiterException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * RateLimiterInterceptor
 */
@Slf4j
public class RateLimiterInterceptor extends HandlerInterceptorAdapter {

    LimitProperties limitProperties;

    RateLimiter globalRateLimiter=null;

    Map<String,RateLimiter> rateLimiterMap=new HashMap<>();

    /**
     * 通过构造函数初始化限速器
     */
    public RateLimiterInterceptor(LimitProperties limitProperties) {
        super();
        this.limitProperties = limitProperties;
        if(limitProperties.isEnabled()){
            initRateLimiters();
        }
    }

    public void initRateLimiters(){
        //全局
        if (Objects.nonNull(limitProperties.getGlobalQps())&&limitProperties.getGlobalQps()>0){
            globalRateLimiter=RateLimiter.create(limitProperties.getGlobalQps());
        }else{
            log.warn("GlobalRateLimiter not init,globalQps - [{}]",limitProperties.getGlobalQps());
        }
        //局部
        Map<String, Double> limitMapping = limitProperties.getSpecialMapping();
        if (!CollectionUtils.isEmpty(limitMapping)){
            limitMapping.forEach((k,v)->{
                if (Objects.nonNull(v)&&v>0){
                    rateLimiterMap.put(k,RateLimiter.create(v));
                }else{
                    log.warn("ignore limit init key-[{}],value-[{}]",k,v);
                }
            });
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri=request.getRequestURI();
        String ua = request.getHeader("User-Agent");
        String method = request.getMethod();

        if (limitProperties.isEnabled()){
            boolean ignore = ignore(uri,ua);
            String special = special(uri);
            if (ignore){
                if (StringUtils.isEmpty(special)){
                    return true;
                }else{
                    if (limitProperties.isIgnorePriority()){
                        return true;
                    }
                }
            }
            //先处理局部
            if (!StringUtils.isEmpty(special)){
                RateLimiter rateLimiter = rateLimiterMap.get(special);
                if(rateLimiter.tryAcquire()) {
                    /**
                     * 成功获取到令牌,再去拿全局的
                     */
                    if (Objects.isNull(globalRateLimiter)){
                        return true;
                    }else{
                        if (globalRateLimiter.tryAcquire()){
                            return true;
                        }else{
                            throw new GlobalLimiterException(method,uri);
                        }
                    }

                }else{
                    throw new SpecialLimiterException(method,uri);
                }
            }

            //在处理全局
            if (Objects.isNull(globalRateLimiter)){
                return true;
            }else{
                if (globalRateLimiter.tryAcquire()){
                    return true;
                }else{
                    throw new GlobalLimiterException(method,uri);
                }
            }
        }

        /**
         * 获取失败，直接响应“错误信息”
         * 也可以通过抛出异常，通过全全局异常处理器响应客户端
         */
        return true;
    }

    /**
     * 根据请求url获取局部限制器 key
     * @param uri
     * @return
     */
    private String special(String uri){
        List<String> keys = rateLimiterMap.keySet().stream()
                .filter(o -> o.contains("**")).collect(Collectors.toList());
        for (String key : keys) {
            String regex="^"+key.replace("**",".*")+"$";
            boolean matches = Pattern.matches(regex, uri);
            if (matches){
                return key;
            }
        }

        List<String> unMatchKeys = rateLimiterMap.keySet().stream()
                .filter(o -> !o.contains("/**")).collect(Collectors.toList());
        for (String key : unMatchKeys) {
            if (key.equals(uri)){
                return key;
            }
        }
        return null;
    }

    private boolean ignore(String uri,String ua) {
        boolean ignoreURL = ignoreURL(uri);
        if (ignoreURL){
            return true;
        }
        return ignoreUA(ua);
    }

    private boolean ignoreUA(String ua){
        List<String> ignoreUas = limitProperties.getIgnoreUas();
        if (CollectionUtils.isEmpty(ignoreUas)||StringUtils.isEmpty(ua)){
            return false;
        }
        ignoreUas=ignoreUas.stream().map(o->o.toUpperCase()).collect(Collectors.toList());
        String uaUpper=ua.toUpperCase();
       return ignoreUas.stream().anyMatch(uaUpper::contains);
    }
    private boolean ignoreURL(String uri) {
        if (CollectionUtils.isEmpty(limitProperties.getIgnoreUrls())){
            return false;
        }
        boolean regx = limitProperties.getIgnoreUrls()
                .stream()
                .filter(o -> o.contains("**"))
                .map(urlP->"^"+urlP.replace("**",".*")+"$")
                .anyMatch(uri::matches);
        if (!regx){
            return limitProperties.getIgnoreUrls().contains(uri);
        }else{
            return true;
        }
    }
}
