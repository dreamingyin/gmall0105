package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.mapper.PmsBaseCatalog1Mapper;
import com.atguigu.gmall.mapper.PmsBaseCatalog2Mapper;
import com.atguigu.gmall.mapper.PmsBaseCatalog3Mapper;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

/**
 * Created by Steven on 2019/12/18.
 */

@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    PmsBaseCatalog1Mapper catalog1Mapper;

    @Autowired
    PmsBaseCatalog2Mapper catalog2Mapper;

    @Autowired
    PmsBaseCatalog3Mapper catalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        List<PmsBaseCatalog1> catalog1List=catalog1Mapper.selectAll();
        return catalog1List;
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2=new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        List<PmsBaseCatalog2> catalog2List=catalog2Mapper.select(pmsBaseCatalog2);
        return catalog2List;
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3=new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        List<PmsBaseCatalog3> catalog3List=catalog3Mapper.select(pmsBaseCatalog3);
        return catalog3List;
    }


}
