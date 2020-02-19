package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsOrder;

/**
 * Created by Steven on 2020/2/10.
 */
public interface OrderService {
    String checkeTradedCode(String memberId,String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTrade);

    void upateOrder(OmsOrder omsOrder);
}
