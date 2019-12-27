package com.atguigu.tmall.user.controller;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by Steven on 2019/12/13.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("getUmsMemberReceiveAddresses")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddress(String memberId){
        List<UmsMemberReceiveAddress> umsMemberReceiveAddress=userService.getUmsMemberReceiveAddress(memberId);
        return umsMemberReceiveAddress;
    }
    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMember=userService.getAllUser();
        return umsMember;
    }

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "Hello User";
    }
}
