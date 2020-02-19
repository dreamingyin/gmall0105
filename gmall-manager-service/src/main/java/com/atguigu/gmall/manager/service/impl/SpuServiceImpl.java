package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Steven on 2019/12/20.
 */

@Service
public class SpuServiceImpl implements SpuService{

    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    PmsProductAtrrMapper pmsProductAtrrMapper;

    @Autowired
    PmsProductAtrrValueMapper pmsProductAtrValueMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;

    @Autowired
    PmsProductSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;


    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo=new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> productInfos=pmsProductInfoMapper.select(pmsProductInfo);
        return productInfos;
    }

    @Override
    public void saveSpuInfo(PmsProductInfo productInfo) {

        pmsProductInfoMapper.insertSelective(productInfo);
        //保存spuSaleAttr
        String spuId = productInfo.getId();
        List<PmsProductSaleAttr> spuSaleAttrList = productInfo.getSpuSaleAttrList();

        for (PmsProductSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setProductId(spuId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            ///保存spuSaleAttrValue
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                spuSaleAttrValue.setProductId(spuId);
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
        List<PmsProductImage> spuImageList = productInfo.getSpuImageList();

        for (PmsProductImage spuImage : spuImageList) {
            spuImage.setProductId(spuId);
            pmsProductImageMapper.insertSelective(spuImage);
        }
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> productSaleAttrs = pmsProductAtrrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr productSaleAttr : productSaleAttrs) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            //销售属性属性ID用的是字典表中的ID不是主键
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductAtrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }
        return productSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage=new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> productImageList = pmsProductImageMapper.select(pmsProductImage);
        return productImageList;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {
      /*  PmsProductSaleAttr productSaleAttr=new PmsProductSaleAttr();
        productSaleAttr.setProductId(productId);
        List<PmsProductSaleAttr> productSaleAttrs = spuSaleAttrMapper.select(productSaleAttr);
        for (PmsProductSaleAttr saleAttr : productSaleAttrs) {
            String saleAttrId = saleAttr.getSaleAttrId();

            PmsProductSaleAttrValue saleAttrValue=new PmsProductSaleAttrValue();
            saleAttrValue.setSaleAttrId(saleAttrId);
            saleAttrValue.setProductId(productId);
            List<PmsProductSaleAttrValue> saleAttrValues = spuSaleAttrValueMapper.select(saleAttrValue);
            saleAttr.setSpuSaleAttrValueList(saleAttrValues);
        }*/

        List<PmsProductSaleAttr> productSaleAttrs =spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
        return productSaleAttrs;
    }
}
