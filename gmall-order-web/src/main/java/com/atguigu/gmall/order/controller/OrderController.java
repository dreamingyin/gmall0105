package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Steven on 2020/2/10.
 */

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //检查交易码
        String success = orderService.checkeTradedCode(memberId, tradeCode);
        if (success.equals("success")) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            omsOrder.setFreightAmount(null);
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("订单备注");
            //将时间戳拼接到订单号
            String outTrade = "JD";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            outTrade += simpleDateFormat.format(new Date());
            //外部订单号
            omsOrder.setOrderSn(outTrade);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setMemberUsername(umsMemberReceiveAddress.getName());
            //当前日期加一天
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DATE, 1);
            Date time = day.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType("0");
            omsOrder.setStatus("0");
            omsOrder.setPayType("0");
            omsOrder.setTotalAmount(totalAmount);

            //根据用户id获取购买商品的列表和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            /**
             * 验价验库存(不替用户做决定)
             * 1.根据用户信息查询当前用户的购物车中商品数据
             * 2.循环购物车中商品对象分装成订单对象(订单详情)
             * 3.每次循环商品校验当前商品库存和价格是否否和购买要求
             */
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    /*外部订单号，用来和其他系统进行交互，比如连接Alipay（支付宝）时使用*/
                    omsOrderItem.setOrderSn(outTrade);
                    omsOrderItem.setProductSkuCode("66666666666666");
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductSn(omsCartItem.getProductSn());
                /*检验价格*/
                    boolean b = skuService.checkPrice(omsOrderItem.getProductSkuId(), omsOrderItem.getProductPrice());
                    if (b == false) {
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    } else {
                        omsOrderItems.add(omsOrderItem);
                    }
                }
            }

            omsOrder.setOmsOrderItems(omsOrderItems);

            //将订单和订单详情写入数据库
            //删除购物车对应的商品
            orderService.saveOrder(omsOrder);
            ModelAndView mv = new ModelAndView("redirect:http://payment.gmall.com:8087/index");
            mv.addObject("outTrade", outTrade);
            mv.addObject("totalAmount", totalAmount);
            //重定向到支付系统
            return mv;
        } else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }

    }

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //将购物车页面转化为结算页面清单
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        //收件人地址集合
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getUmsMemberReceiveAddress(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            //每循环一个购物车对象，就封装一个商品到订单详情omsOrderItems
            if (omsCartItem.getIsChecked().equals("1")) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }

        }

        //总金额
        modelMap.put("totalAmount", getTotalAmount(omsCartItems));

        modelMap.put("userAddressList", umsMemberReceiveAddresses);

        modelMap.put("omsOrderItems", omsOrderItems);

        modelMap.put("nickName", nickname);

        //生成交易码，为了再提交订单时校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "Trade";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {

            System.out.println(omsCartItem.getIsChecked());
            if (omsCartItem.getIsChecked().equals("1")) {
                BigDecimal quantity = omsCartItem.getQuantity();
                BigDecimal price = omsCartItem.getPrice();
                totalAmount = totalAmount.add(quantity.multiply(price));
            }

        }
        return totalAmount;
    }
}


