package com.atguigu.gmall.util;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Steven on 2020/1/2.
 */
public class RedisUtil {
    private JedisPool jedisPool;

    public void initPool(String host,int port,String password ,int database){
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(30);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(10*1000);
        poolConfig.setTestOnBorrow(true);
        //jedisPool=new JedisPool(poolConfig,host,port,20*1000);
        jedisPool=new JedisPool(poolConfig,host,port,20*1000, password,database);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

}
