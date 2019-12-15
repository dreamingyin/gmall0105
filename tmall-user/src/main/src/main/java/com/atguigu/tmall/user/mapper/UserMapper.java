package com.atguigu.tmall.user.mapper;

import com.atguigu.tmall.user.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Steven on 2019/12/13.
 */
public interface UserMapper extends Mapper<UmsMember>{


    List<UmsMember> selectAllUser();

}
