package com.atguigu.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steven on 2019/12/29.
 */

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, ModelMap map, HttpServletRequest request){
        String remoteAddr = request.getRemoteAddr();
        PmsSkuInfo skuInfo=skuService.getSkuById(skuId,remoteAddr);
        //销售属性对象
       map.put("skuInfo",skuInfo);
        //销售属性列表
        List<PmsProductSaleAttr> productSaleAttrs=spuService.spuSaleAttrListCheckBySku(skuInfo.getProductId(),skuInfo.getId());
        map.put("spuSaleAttrListCheckBySku",productSaleAttrs);

        //查询当前sku的spu的集合hash表
        Map<String,String> saleAttrHash=new HashMap();
        List<PmsSkuInfo> skuInfos = skuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());
        for (PmsSkuInfo pmsSkuInfo : skuInfos) {
            String k="";
            String v=pmsSkuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                k+=saleAttrValue.getSaleAttrValueId()+"|";
            }
            saleAttrHash.put(k,v);
        }
        //将sku的销售属性放到页面
        String saleAttrHashJsonStr = JSON.toJSONString(saleAttrHash);
        map.put("saleAttrHashJsonStr",saleAttrHashJsonStr);
        return "item";
    }

    @RequestMapping("index")
    public String index(ModelMap modelMap){
        List<String> list=new ArrayList<>();
        for (int i = 0; i <5; i++) {
            list.add("循环数据："+i);
        }
        modelMap.put("list",list);
        modelMap.put("check","1");
        modelMap.put("hello","Hello Thymeleaf!!!!");
        return "index";
    }
}
