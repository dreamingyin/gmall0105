package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.service.BaseAttrInfoService;
import com.atguigu.gmall.service.BaseSaleAttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Steven on 2019/12/18.
 */

@CrossOrigin
@Controller
public class AtrrController {

    @Reference
    BaseAttrInfoService baseAtrrInfoService;


    @Reference
    BaseSaleAttrService baseSaleAttrService;

    /**
     * 获取商品信息
     *
     * @return
     */

    /**
     * 查询商品的销售属性
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> baseAttrInfos=baseSaleAttrService.baseSaleAttrList();
        return baseAttrInfos;
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){

        List<PmsBaseAttrInfo> baseAttrInfos= baseAtrrInfoService.getAttInfo(catalog3Id);
        return baseAttrInfos;
    }


    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        String success= baseAtrrInfoService.saveAttrInfo(pmsBaseAttrInfo);
        return success;
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue>getAttrValueList(String attrId){
        List<PmsBaseAttrValue> pmsBaseAttrValues= baseAtrrInfoService.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }

}
