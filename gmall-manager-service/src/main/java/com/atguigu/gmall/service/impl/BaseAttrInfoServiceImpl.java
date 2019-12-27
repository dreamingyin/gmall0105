package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.mapper.BaseAtrrInfoMapper;
import com.atguigu.gmall.mapper.BaseAtrrInfoValueMapper;
import com.atguigu.gmall.service.BaseAtrrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 2019/12/18.
 */

@Service
public class BaseAttrInfoServiceImpl implements BaseAtrrInfoService {

    @Autowired
    BaseAtrrInfoMapper baseAtrrInfoMapper;

    @Autowired
    BaseAtrrInfoValueMapper baseAtrrInfoValueMapper;
    @Override
    public List<PmsBaseAttrInfo> getAttInfo(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo=new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = baseAtrrInfoMapper.select(pmsBaseAttrInfo);
        List<PmsBaseAttrValue> pmsBaseAttrValues=new ArrayList<>();
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            pmsBaseAttrValues=baseAtrrInfoValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }
    
        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id=pmsBaseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
            baseAtrrInfoMapper.insertSelective(pmsBaseAttrInfo);

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                baseAtrrInfoValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else{

            //修改属性
            Example example=new Example(PmsBaseAttrValue.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            baseAtrrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            //根据PmsBaseAttrValue的setAttrId删除所有属性值
            PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            baseAtrrInfoValueMapper.delete(pmsBaseAttrValue);

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                baseAtrrInfoValueMapper.insertSelective(baseAttrValue);
            }

        }

        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = baseAtrrInfoValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

}
