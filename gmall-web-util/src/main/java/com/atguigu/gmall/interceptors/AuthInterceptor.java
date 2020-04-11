package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // handler方法向下转型
        HandlerMethod hm = (HandlerMethod) handler;
        // 获取拦截的方法的注解
        LoginRequired loginRequired = hm.getMethodAnnotation(LoginRequired.class);
        // 判断loginRequired是否为空
        if(loginRequired == null){
            // 为空，表示该方法不需要拦截
            return true;
        }
        // 声明token
        String token = "";
        // 获取Cookie中的oldToken
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        // 获取request中的新token
        String newToken = request.getParameter("newToken");
        /* token有四种状态：
        oldToken和newToken都为空——未登录
        oldToken为空，newToken不为空——刚刚登陆
        oldToken不为空，newToken为空——之前登陆过
        oldToken和newToken都不为空——登陆过期
        */
        // 判断四种状态
        if(StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }

        if(StringUtils.isNotBlank(newToken)){
            token = newToken;
        }

        // 判断token是否有效
        if(StringUtils.isNotBlank(token)){
            String ip = request.getRemoteAddr();
            // 有效，验证token(不去中心化)
            String json = HttpclientUtil.doGet("http://passport.gmall.com:8087/verify?token=" + token);
            Map mapResult = new HashMap();
            Map map = JSON.parseObject(json, mapResult.getClass());
            // 有效，验证token(去中心化)
            // 解析Jwt
            //String key = "atguigu0830";
            //Map map = JwtUtil.decode(key,token,ip);
            String userId = (String)map.get("userId");
            String nickname = (String)map.get("nickname");
            // 判断验证结果
            if(StringUtils.isNotBlank(userId)){
                // 验证通过，将token重新写入访问请求的客户端的cookie中，生成新的通行证凭据，更新通行证凭据的过期时间
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*24,true);
                request.setAttribute("userId",userId);
                request.setAttribute("nickname",nickname);
                return true;
            }
        }
        boolean result = loginRequired.ifMustLogin();
        if(result){
            // 无效，去登录页面
            // 获取原始请求路径
            StringBuffer ReturnUrl = request.getRequestURL();
            response.sendRedirect("http://passport.gmall.com:8087/index?ReturnUrl=" + ReturnUrl);
            return false;
        }
        return true;
    }
}
