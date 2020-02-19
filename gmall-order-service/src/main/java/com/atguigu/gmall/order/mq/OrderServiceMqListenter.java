package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @FileName: OrderServiceMqListenter
 * @Author Steven
 * @Date: 2020/2/15
 */
@Component
public class OrderServiceMqListenter {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {

        String out_trade_no = mapMessage.getString("out_trade_no");

        //更新订单业务
        System.out.println(out_trade_no);

        OmsOrder omsOrder=new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);

        orderService.upateOrder(omsOrder);

        System.out.println("22222222222222222");

    }

}
