package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;

import java.util.List;

public interface AttrInfoService {

    /**
     *  用于查询平台属性信息
     * @param catalog3Id 三级分类的id
     * @return List<PmsBaseAttrInfo>集合
     */
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    /**
     *  用于保存或修改平台信息
     * @param pmsBaseAttrInfo 需要保存或修改的信息
     */
    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    /**
     *  根据attrId查询PmsBaseAttrInfo对象
     * @param attrId
     * @return PmsBaseAttrInfo对象
     */
    PmsBaseAttrInfo getAttrValueList(String attrId);
}
