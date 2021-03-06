package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.manager.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.service.BaseSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Steven on 2019/12/20.
 */
@Service
public class BaseSaleAttrServiceImpl implements BaseSaleAttrService{

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;


    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseAttrValues=baseSaleAttrMapper.selectAll();
        return pmsBaseAttrValues;
    }
}
