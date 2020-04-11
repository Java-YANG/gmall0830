package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.passport.util.JwtUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(HttpServletRequest request,String code){
        // 第三个url
        // 根据授权登录时生成的code交换access_token,交换access_token必须使用post请求
        String accessTokenUrl = "https://api.weibo.com/oauth2/access_token";
        Map<String,String> accessTokenMap = new HashMap<>();
        accessTokenMap.put("client_id","248927699");
        accessTokenMap.put("client_secret","365bda51aef3a97ffb10b8111464e4c4");
        accessTokenMap.put("grant_type","authorization_code");
        accessTokenMap.put("redirect_uri","http://passport.gmall.com:8087/vlogin");
        accessTokenMap.put("code",code);// code存在一定的过期时间，大约5min

        // 获得access_token
        String json = HttpclientUtil.doPost(accessTokenUrl,accessTokenMap);
        // 判断获取是否成功
        if(StringUtils.isNotBlank(json)){
            // 获取成功
            Map<String,Object> jsonMap = new HashMap<>();
            jsonMap = JSON.parseObject(json,jsonMap.getClass());
            // 获取access_token
            String access_token = (String)jsonMap.get("access_token");// access_token一般可以用三天左右
            // 获取uid
            String uid = (String)jsonMap.get("uid");// uid是用户在第三方的id

            // 第四个url
            // 根据access_token(数据交换码)和uid(用户在第三方的id)查询用户在第三方上的用户数据
            String showUserUrl = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
            // 获取用户在第三方用户数据
            String userJson = HttpclientUtil.doGet(showUserUrl);
            jsonMap = JSON.parseObject(userJson,jsonMap.getClass());
            // 将用户数据保存到本地项目数据库中，并更新缓存
            UmsMember umsMember = new UmsMember();
            umsMember.setCreateTime(new Date());
            umsMember.setAccessCode(code);
            umsMember.setAccessToken(access_token);
            umsMember.setCity((String)jsonMap.get("city"));
            umsMember.setNickname((String)jsonMap.get("screen_name"));
            umsMember.setSourceUid(uid);
            umsMember.setSourceType("2");
            umsMember.setStatus(1);
            // 保存用户信息，并返回生成的主键id
            UmsMember umsMemberResult = userService.addVloginUser(umsMember);

            // 生成token
            // 第一部分
            String key = "atguigu0830";
            // 第二部分
            Map<String,String> tokenMap = new HashMap<>();
            tokenMap.put("userId",umsMemberResult.getId());
            tokenMap.put("nickname",umsMemberResult.getNickname());
            // 第三部分
            String ip = "";
            ip = request.getHeader("X-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            // 调用Jwt生成token
            String token = JwtUtil.encode(key,tokenMap,ip);

            // 同步缓存
            userService.putToken(token,umsMemberResult);

            // 重定向到首页
            return "redirect:http://search.gmall.com:8084/index?newToken=" + token;
        }
        return "index";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,HttpServletRequest request){
        // 验证token，调用userService层服务或缓存
        UmsMember umsMember = userService.verify(token);
        // 判断验证结果
        if(umsMember == null)
            //验证失败
            return null;
        Map mapResult = new HashMap();
        mapResult.put("userId",umsMember.getId());
        mapResult.put("nickname",umsMember.getNickname());
        return JSON.toJSONString(mapResult);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request,String username,String password){
        // 调用userServer层的服务进程用户的用户名和密码验证
        UmsMember umsMemberFromDb = userService.login(username,password);

        if(umsMemberFromDb == null){
            return "fail";
        }
        // 生成token,使用jwt
        // 第一部分：公共部分key
        String key = "atguigu0830";
        // 第二部分：私人信息部分map
        Map<String,String> map = new HashMap<>();
        map.put("userId",umsMemberFromDb.getId());
        map.put("nickname",umsMemberFromDb.getNickname());
        // 第三部分：签名部分：网络防伪
        String ip = request.getRemoteAddr();
        // 生成token
        String token = JwtUtil.encode(key,map,ip);

        // 同步缓存
        userService.putToken(token,umsMemberFromDb);
        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){

        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }

}
