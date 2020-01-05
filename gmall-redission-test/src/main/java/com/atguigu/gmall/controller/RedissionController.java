package com.atguigu.gmall.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.atguigu.gmall.util.RedisUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

/**
 * Created by Steven on 2020/1/5.
 */
@Controller
public class RedissionController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("testRedission")
    @ResponseBody
    public String testRedission(){
        /*RLock lock = redissonClient.getLock("lock");*/
        Jedis jedis = redisUtil.getJedis();
        String v=jedis.get("k");
        if(StringUtils.isBlank(v)){
            v="1";
        }
        System.out.println("---->"+v);
        jedis.set("k",(Integer.parseInt(v)+1)+"");
        jedis.close();
        return "success";
    }
}