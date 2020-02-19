package com.atguigu.gmall.util;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steven on 2020/2/3.
 */
public class Test {

    public static void main(String[] args) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("name","steven");
        hashMap.put("password","324756");
        String ip="127.0.0.1";
        Date date=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(date);
        String encode = JwtUtil.encode("2019gmall0105", hashMap, ip + format);
        System.out.println(encode);


        //String tokenUserInfo = StringUtils.substringBetween(encode, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode("eyJwYXNzd29yZCI6IjMyNDc1NiIsIm5hbWUiOiJzdGV2ZW4ifQ");
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        System.out.println("64="+map);

        System.exit(0);

    }
}
