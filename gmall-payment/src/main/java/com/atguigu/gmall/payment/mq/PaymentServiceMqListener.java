package com.atguigu.gmall.payment.mq;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.manager.mq.ActiveMQUtil;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * @FileName: PaymentServiceMqListener
 * @Author Steven
 * @Date: 2020/2/16
 */
@Component
public class PaymentServiceMqListener {

    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymenChecktResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        Integer count=0;
        if(mapMessage.getString("count")!=null){
            count = Integer.parseInt(""+mapMessage.getString("count"));

        }

        //调用PaymentService的支付宝检查的接口
        System.err.println("进行延迟检查，调用支付检查的接口服务");
        Map<String,Object> resultMap=paymentService.checkAlipayPayment(out_trade_no);

        if(resultMap!=null&&!resultMap.isEmpty()){

            String trade_status=(String) resultMap.get("trade_status");
            //根据查询的状态结果，判断是否进行下一次的延迟任务还是支付成功和后续

            if(StringUtils.isNotBlank(trade_status)&&trade_status.equals("TRADE_SUCCESS")){
                //支付成功，更新支付服务发送支付队列

                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                //支付宝的交易凭证号
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String)resultMap.get("trade_no"));
                paymentInfo.setCallbackTime(new Date());
                //回调请求字符串
                paymentInfo.setCallbackContent((String)resultMap.get("call_back_content"));
                paymentService.updatePayment(paymentInfo);
                //支付成功，和发送支付成功队列
                System.err.println("已支付成功，调用支付服务，修改支付信息和发送支付成功队列");
                return;
            }
        }

        if(count>0) {
            //继续发送延迟检查任务，计算延迟时间等
            System.out.println("没有支付成功，检查剩余次数"+count+",继续发送延迟检查任务");
            count--;
            paymentService.sendDelayPaymentResultCheckQueue(out_trade_no, count);
        }else{
            System.err.println("检查次数用尽！！！");
        }


    }

}
