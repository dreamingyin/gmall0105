package com.atguigu.gmall.payment.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.manager.mq.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steven on 2020/2/14.
 */
@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insert(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        //幂等性检查
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoResult=paymentMapper.selectOne(paymentInfoParam);
        if(StringUtils.isNotBlank(paymentInfoResult.getPaymentStatus())&&paymentInfoResult.getPaymentStatus().equals("已支付")){
            return;
        }else{

            String orderSn = paymentInfo.getOrderSn();
            Example example=new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn",orderSn);
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            Session session=null;
            Connection connection=null;
            try {
                connection = connectionFactory.createConnection();
                session = connection.createSession(true, Session.SESSION_TRANSACTED);

            } catch (JMSException e) {
                e.printStackTrace();
            }

            try{
                paymentMapper.updateByExampleSelective(paymentInfo,example);
                //支付成功后，引起系统服务===》订单服务===》库存服务===》物流
                //调用mq发送支付成功的消息
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payment_success_queue);

                //字符串文本
                //TextMessage textMessage=new ActiveMQTextMessage();

                //hash结构
                MapMessage mapMessage=new ActiveMQMapMessage();
                mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
                producer.send(mapMessage);
                session.commit();

            }catch (Exception e){
                //消息回滚
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            }finally {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTrade,int count) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Session session=null;
        Connection connection=null;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);

        } catch (JMSException e) {
            e.printStackTrace();
        }

        try{

            Queue payment_success_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //字符串文本
            //TextMessage textMessage=new ActiveMQTextMessage();

            //hash结构
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",outTrade);
            mapMessage.setInt("count",count);
            //为消息加入延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*60);
            producer.send(mapMessage);
            session.commit();

        }catch (Exception e){
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(resultMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("交易创建已创建，调用成功");
            resultMap.put("out_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            resultMap.put("call_back_content",response.getMsg());
        } else {
            System.out.println("有可能交易创建未创建，调用失败");
        }
        return resultMap;
    }
}
