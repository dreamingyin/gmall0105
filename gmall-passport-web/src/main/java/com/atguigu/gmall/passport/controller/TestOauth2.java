package com.atguigu.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steven on 2020/2/7.
 */
public class TestOauth2 {


    public static String getCode(){
       // App Key：3193708961
//        App Secret：df2b2370fbd3483cdd8a9eb422ca325
//        授权回调地址:  http://passport.gmall.com:8085/vlogin
        String s = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=3193708961&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin");
        System.out.println(s);

        //在第一步与第二步之间由用户授权的过程
        //29d0ce2aa8465049c9e2c6ce27afe830

        String s2="http://passport.gmall.com:8085/vlogin?code=29d0ce2aa8465049c9e2c6ce27afe830";
        System.out.println(s2);

        return null;

    }

    public static String access_token(){

        String s3="https://api.weibo.com/oauth2/access_token?";
        //?client_id=3193708961&client_secret=df2b2370fbd3483cdd8a9eb422ca325&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=CODE";

        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id","3193708961");
        paramMap.put("client_secret","df2b2370fbd3483cdd8a9eb422ca3253");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code","6796ce1da0ff67e07274c31a21621abe");

        String access_token_json = HttpclientUtil.doPost(s3, paramMap);
        Map<String,String> access_map = JSON.parseObject(access_token_json, Map.class);
        //授权码：2.002zsK3GraTIUD9a663c36d4NG8jHC
        System.out.println(access_map.get("access_token"));
        //uid：5824471043
        System.out.println(access_map.get("uid"));

        return access_map.get("access_token");
    }

    public static Map<String, String> getUser_info(){

        String s4="https://api.weibo.com/2/users/show.json?access_token=2.002zsK3GraTIUD9a663c36d4NG8jHC&uid=5824471043";
        String s = HttpclientUtil.doGet(s4);
        Map<String,String> map = JSON.parseObject(s, Map.class);
        return map;

    }

    public static void main(String[] args) {

        getUser_info();
    }
}
