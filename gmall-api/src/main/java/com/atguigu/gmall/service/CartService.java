package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {

    OmsCartItem isExistOmsCartItem(String userId, String productSkuId);

    void updateOmsCartItem(OmsCartItem omsCartItemForUser);

    void insertOmsCartItem(OmsCartItem omsCartItem);

    List<OmsCartItem> getOmsCartItemByUserId(String userId);

    void updateCartChecket(String skuId, String userId, String isChecked);

    List<OmsCartItem> getCartsByUserId(String userId);

    void deleteBuyCarts(String userId);
}
