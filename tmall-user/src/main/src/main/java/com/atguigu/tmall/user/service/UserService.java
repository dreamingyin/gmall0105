package com.atguigu.tmall.user.service;

import com.atguigu.tmall.user.bean.UmsMember;
import com.atguigu.tmall.user.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * Created by Steven on 2019/12/13.
 */
public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getUmsMemberReceiveAddress(String memberId);

}
