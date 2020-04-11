package com.atguigu.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.util.List;
import java.util.Random;

/**
 * 随机均匀算法
 * 随机，按权重设置随机概率
 * 在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private static final String NAME = "random";
    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        // provider的数量
        int length = invokers.size(); // 集合数量
        int totalWeight = 0; // 权重值总和
        boolean sameWeight = true; // 每一个invoker都有相同的权重值
        for (int i = 0; i < length; i++) {
            // 获得集合中每个元素的权重值
            int weight = getWeight(invokers.get(i),invocation);
            // 得到总权重值
            totalWeight += weight;
            if(sameWeight && i > 0 && weight != getWeight(invokers.get(i - 1),invocation)){
                sameWeight = false;
            }
        }
        if(totalWeight > 0 && !sameWeight){
            // 如果(不是每个调用者都有相同的重量&至少有一个调用者的重量>0)，根据总重量随机选择
            int offset = random.nextInt(totalWeight);
            // Return a invoker based on the random value.
            // 可以理解成：[0,totalWeight)取随机数，看这个随机数(每比较一次，减去响应的权重)
            // 落在了以权重为刻度的数轴哪个区间内，落在那个区间即返回哪个provider
            for (int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i),invocation);
                if(offset <  0){
                    return invokers.get(i);
                }
            }
        }
        // 如果所有调用者都具有相同的权值或totalWeight=0，则均匀返回
        return invokers.get(random.nextInt(length));
    }
}
