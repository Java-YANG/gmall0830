package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ProductInfoServiceImpl implements ProductInfoService {
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;


    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        // 创建一个空的PmsProductInfo对象
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        // 将参数catalog3Id封装到PmsProductInfo对象
        pmsProductInfo.setCatalog3Id(catalog3Id);
        // 调用pmsProductInfoMapper层的查询方法
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        // 返回查询结果
        return pmsProductInfos;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        // 查询销售字典表信息并返回
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        // 调用pmsProductInfoMapper层的保存方法
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        // 获取保存pmsProductInfo后的主键
        String pmsProductInfoId = pmsProductInfo.getId();

        // 获取spuSaleAttrList
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();

        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
            // 设置spu主键
            pmsProductSaleAttr.setProductId(pmsProductInfoId);

            // 保存spu销售属性
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            // 获取销售使用的字典表主键
            String saleAttrId = pmsProductSaleAttr.getSaleAttrId();

            // 获取spu销售属性值集合
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();

            for (PmsProductSaleAttrValue pmsProductSaleAttrValue:spuSaleAttrValueList) {
                // 设置spu主键
                pmsProductSaleAttrValue.setProductId(pmsProductInfoId);
                // 设置销售使用的字典表主键
                pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
                // 调用pmsProductSaleAttrValueMapper执行保存，spu主键和销售字典表主键为联合外键
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }

        }
        // 获取PmsProductImage集合对象
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        // 遍历集合
        for (PmsProductImage pmsProductImage : spuImageList) {
            // 设置spu主键
            pmsProductImage.setProductId(pmsProductInfoId);
            // 调用pmsProductImageMapper层的保存方法
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }

    }

    @Override
    public List<PmsProductSaleAttr> productInfoService(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }


}
