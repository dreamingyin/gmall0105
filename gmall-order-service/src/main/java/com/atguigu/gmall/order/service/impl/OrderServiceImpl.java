package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.manager.mq.ActiveMQUtil;
import com.atguigu.gmall.manager.util.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderItemMapper;
import com.atguigu.gmall.order.mapper.OrderMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Steven on 2020/2/11.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderMapper omsOrderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Reference
    CartService cartService;

    @Override
    public String checkeTradedCode(String memberId,String tradeCode) {
        Jedis jedis =null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";

            //并发情况下的一key多用，使用lua脚本在查询到该key的时候，马上删除，发现立即击毙
            String tradeCodeFromCache = jedis.get(tradeKey);

            //对比防重删令牌
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCode));
            if (eval!=null&eval!=0) {
                //jedis.del("user:" + memberId + ":tradeCode");
                return "success";
            } else {
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey="user:"+memberId+":tradeCode";
        String tradeCode= UUID.randomUUID().toString();
        jedis.setex(tradeKey,60*15,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            //保存订单详情
            orderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据
            //cartService.delCart();
        }

    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTrade) {
        OmsOrder omsOrder=new OmsOrder();
        omsOrder.setOrderSn(outTrade);
        OmsOrder order = omsOrderMapper.selectOne(omsOrder);
        return order;
    }

    @Override
    public void upateOrder(OmsOrder omsOrder) {
        Example e=new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate=new OmsOrder();
        omsOrderUpdate.setStatus("1");


        //发送一个订单已支付的队列，提供给库存消费
        Session session=null;
        Connection connection=null;

        try{
            connection= activeMQUtil.getConnectionFactory().createConnection();
            session=connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //hash结构
            MapMessage mapMessage=new ActiveMQMapMessage();
            omsOrderMapper.updateByExampleSelective(omsOrderUpdate,e);
            producer.send(mapMessage);
            session.commit();

        }catch (Exception eX){
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException el) {
                el.printStackTrace();
            }
        }

    }

}
