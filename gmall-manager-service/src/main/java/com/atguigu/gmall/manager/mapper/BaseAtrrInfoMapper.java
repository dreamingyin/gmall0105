package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Steven on 2019/12/18.
 */
public interface BaseAtrrInfoMapper extends Mapper<PmsBaseAttrInfo>{

    List<PmsBaseAttrInfo> selectAttrValueListValueId(@Param("valueIdStr") String valueIdStr);
}
