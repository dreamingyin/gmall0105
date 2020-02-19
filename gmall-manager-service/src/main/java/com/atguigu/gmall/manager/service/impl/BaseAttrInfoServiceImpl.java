package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.manager.mapper.BaseAtrrInfoMapper;
import com.atguigu.gmall.manager.mapper.BaseAtrrInfoValueMapper;
import com.atguigu.gmall.service.BaseAttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Steven on 2020/1/22.
 */

@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService{

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

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> attrIdSet) {
        String valueIdStr = StringUtils.join(attrIdSet, ",");
        List<PmsBaseAttrInfo> baseAttrInfos=baseAtrrInfoMapper.selectAttrValueListValueId(valueIdStr);
        return baseAttrInfos;
    }
}
