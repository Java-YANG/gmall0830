package com.atguigu.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcStatus;

import java.util.List;
import java.util.Random;

/**
 * 最少活跃调用数均衡算法
 * 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
 * 使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。
 *
 * 最少活跃数调用法：这个方法的主要作用根据服务的提供者的运行状态去选择服务器,主要的思路就是遍历每个调用者，然后获取每个
 * 服务器的运行状态，如果当前运行的运行状态小于最小的状态-1，把它保存在leastIndexs中的第一个位置，并且认定所有的调用者权
 * 重都相同，然后直接返回那个调用者(这里的逻辑是：找到最少活跃数(在代码层反应就是：active的值))。如果计算出的权重值和最
 * 少的权重值相同，那么把它保存在leastIndexs数组里面，累加权重值，如果当前的权重值不等于初始值firstWeight，那么就认定不
 * 是所有的调用者的权重不同。然后再遍历lestIndexs，取权重累加值的随机数生成权重偏移量，在累减它，到它小于0的时候返回那个
 * 调用者。如果这些都不符合，就从leastIndexs随机选一个index，返回那个调用者！
 */
public class LeastActionLoadBalance extends AbstractLoadBalance{
    public static final String NAME = "leastactive";
    private final Random random = new Random();
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size(); // Number of invokers
        int leastActive = -1; // The least active value of all invokers
        int leastCount = 0; // The number of invokers having the same least active value (leastActive)
        int[] leastIndexs = new int[length]; // The index of invokers having the same least active value (leastActive)
        int totalWeight = 0; // The sum of weights
        int firstWeight = 0; // Initial value, used for comparision
        boolean sameWeight = true; // Every invoker has the same weight value?
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive(); // Active number
            int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT); // Weight
            if (leastActive == -1 || active < leastActive) { // Restart, when find a invoker having smaller least active value.
                leastActive = active; // Record the current least active value
                leastCount = 1; // Reset leastCount, count again based on current leastCount
                leastIndexs[0] = i; // Reset
                totalWeight = weight; // Reset
                firstWeight = weight; // Record the weight the first invoker
                sameWeight = true; // Reset, every invoker has the same weight value?
            } else if (active == leastActive) { // If current invoker's active value equals with leaseActive, then accumulating.
                leastIndexs[leastCount++] = i; // Record index number of this invoker
                totalWeight += weight; // Add this invoker's weight to totalWeight.
                // If every invoker has the same weight?
                if (sameWeight && i > 0
                        && weight != firstWeight) {
                    sameWeight = false;
                }
            }
        }
        // assert(leastCount > 0)
        if (leastCount == 1) {
            // If we got exactly one invoker having the least active value, return this invoker directly.
            return invokers.get(leastIndexs[0]);
        }
        if (!sameWeight && totalWeight > 0) {
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
            int offsetWeight = random.nextInt(totalWeight);
            // Return a invoker based on the random value.
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexs[i];
                offsetWeight -= getWeight(invokers.get(leastIndex), invocation);
                if (offsetWeight <= 0)
                    return invokers.get(leastIndex);
            }
        }
        // If all invokers have the same weight value or totalWeight=0, return evenly.
        return invokers.get(leastIndexs[random.nextInt(leastCount)]);
    }
}
