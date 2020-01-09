package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface ProductInfoService {
    /**
     *  根据三级id查询商品属性SPU
     * @param catalog3Id
     * @return List<PmsProductInfo>集合
     */
    List<PmsProductInfo> spuList(String catalog3Id);

    /**
     *  查询销售属性
     * @return List<PmsBaseSaleAttr>集合
     */
    List<PmsBaseSaleAttr> baseSaleAttrList();

    /**
     *  保存SUP
     * @param pmsProductInfo
     */
    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> productInfoService(String spuId);

    List<PmsProductImage> spuImageList(String spuId);
}
