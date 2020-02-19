package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steven on 2020/2/13.
 */
@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;

    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public  String aliPaycallBackReturn(HttpServletRequest request, ModelMap modelMap){

        //更新用户的支付状态
        String sign = request.getParameter("sign");
        String trade_no=request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");

        String call_back_content = request.getQueryString();

        //通过支付宝的paymentMap进行签名验证，2.0版本中将接口paymentMap参数去掉了，导致同步请求无法验签
        if(StringUtils.isNotBlank(sign)){
            //验签成功
            //进行支付更新的幂等性检查操作
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            //支付宝的交易凭证号
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackTime(new Date());
            //回调请求字符串
            paymentInfo.setCallbackContent(call_back_content);
            //更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }

        //支付成功后，引起系统服务===》订单服务===》库存服务===》物流
        //调用mq发送支付成功的消息
        return "finish";
    }

    @RequestMapping("wx/submit")
    @LoginRequired(loginSuccess = true)
    public  String wxPay(String outTrade, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        return "";
    }

    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public  String aliPay(String outTrade, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        //获得支付宝客户端,不是一个链接而是针对http的表单请求
        String form="";
        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        //同步回调地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> map=new HashMap<>();
        map.put("out_trade_no",outTrade);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject","JD Apple 10 phone");
        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //生成用户的支付信息
        OmsOrder omsOrder=orderService.getOrderByOutTradeNo(outTrade);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTrade);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("JD商品");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo );

        //向消息中间件发送一个检查支付状态(支付服务的延迟消息队列)
        paymentService.sendDelayPaymentResultCheckQueue(outTrade,5);

        //提交请求到支付宝
        return form;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public  String index(String outTrade, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");
        modelMap.put("memberId",memberId);
        modelMap.put("outTrade",outTrade);
        modelMap.put("nickName",nickName);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }
}
