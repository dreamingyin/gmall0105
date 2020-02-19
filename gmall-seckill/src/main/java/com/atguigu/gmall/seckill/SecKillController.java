package com.atguigu.gmall.seckill;

import com.atguigu.gmall.manager.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * @FileName: SecKillController
 * @Author Steven
 * @Date: 2020/2/17
 */
@RestController
public class SecKillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 先到先得式秒杀
     * @return
     */
    @RequestMapping("seckillRedission")
    public String secKillRedission() {
        Jedis jedis = redisUtil.getJedis();
        Integer stock = Integer.parseInt(jedis.get("126"));
        RSemaphore semaphore = redissonClient.getSemaphore("126");
        boolean b = semaphore.tryAcquire();
        if (b) {
            System.out.println("当前库存剩余数量：" + stock + "抢购成功,当前人数：" + (1000 - stock));
            System.out.println("消息对列发出订单");
        } else {
            System.out.println("当前库存剩余数量：" + stock + "抢购失败");
        }
        jedis.close();

        return "1";
    }

    /**
     * 运气式秒杀
     * @return
     */
    @RequestMapping("seckill")
    public String secKill() {
        Jedis jedis = redisUtil.getJedis();
        //开启商品的监控
        jedis.watch("126");
        Integer stock = Integer.parseInt(jedis.get("126"));
        if (stock > 0) {
            Transaction multi = jedis.multi();
            multi.decrBy("126", 1);
            List<Object> exec = multi.exec();
            if (exec != null && exec.size() > 0) {
                System.out.println("当前库存剩余数量：" + stock + "抢购成功,当前人数：" + (1000 - stock));
            } else {
                System.out.println("当前库存剩余数量：" + stock + "抢购失败");
            }
        }

        jedis.close();
        return "1";
    }

}
