package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUserMember();

    UmsMember verify(String token);

    UmsMember login(String username, String password);

    void putToken(String token, UmsMember umsMemberFromDb);

    List<UmsMemberReceiveAddress> ReceiverAddresseByUserId(String userId);

    UmsMember addVloginUser(UmsMember umsMember);

    UmsMemberReceiveAddress getReceiveAddress(String addressId);
}
