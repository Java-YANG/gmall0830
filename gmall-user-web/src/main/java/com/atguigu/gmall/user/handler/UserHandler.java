package com.atguigu.gmall.user.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserHandler {

    @Reference
    private UserService userService;

    @ResponseBody
    @RequestMapping("get/all/user/member")
    public List<UmsMember> getAllUserMember(){

        return userService.getAllUserMember();
    }

}
