package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.service.AttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AttrInfoServiceImpl implements AttrInfoService {
    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        // 创建PmsBaseAttrInfo对象
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();

        // 将catalog3Id封装到PmsBaseAttrInfo对象中
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);

        // 调用pmsBaseAttrInfoMapper层的查询方法
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        // 返回查询结果
        return pmsBaseAttrInfos;
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        // 获取传入的PmsBaseAttrInfo对象的id
        String id = pmsBaseAttrInfo.getId();
        // 判断attrInfoId是否存在
        if(id == null || "".equals(id)){// StringUtils.isBlank(id)
            // 添加操作
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            // 获取添加后的主键
            id = pmsBaseAttrInfo.getId();
        }else {
            // 修改属性
            // 创建Example对象
            Example example = new Example(PmsProductInfo.class);
            // 创建Criteria对象
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            // 调用pmsBaseAttrInfoMapper层的修改方法
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            // 删除属性值
            // 创建一个空的属性值对象
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            // 设置属性id
            pmsBaseAttrValue.setAttrId(id);
            // 执行删除属性值，根据属性id进行删除
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
        }
        // 获取需要保存或修改的属性值集合
        List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
        // 遍历集合
        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
            // 设置属性id
            pmsBaseAttrValue.setAttrId(id);
            // 选中性保存属性值
            pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
        }
    }

    @Override
    public PmsBaseAttrInfo getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> attrValueList = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

        PmsBaseAttrInfo pmsBaseAttrInfo = pmsBaseAttrInfoMapper.selectByPrimaryKey(attrId);
        pmsBaseAttrInfo.setAttrValueList(attrValueList);

        return pmsBaseAttrInfo;
    }

}
