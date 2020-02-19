package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Steven on 2019/12/27.
 */
public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo>{
    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(String productId);
}
