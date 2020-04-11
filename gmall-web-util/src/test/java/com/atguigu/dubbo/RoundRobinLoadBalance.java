package com.atguigu.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.AtomicPositiveInteger;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 权重轮循均衡算法
 * 轮循，按公约后的权重设置轮循比率。
 * 存在慢的提供者累积请求问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，
 * 久而久之，所有请求都卡在调到第二台上。(针对此种情况，需要降低该服务的权值，以减少对其调用)
 *
 * 轮询调用，轮询调用的过程主要是维护了局部变量的一个LinkdesHashMap（有顺序的Map）去存储调用者和权重值的对应关系，
 * 然后遍历每个调用者,把调用者和当前大于0的权重值放进去，再累加权重值。还有一个全局变量的map，找到第一个服务调用者，
 * 首先是找到每个服务的key值和method，这里可以理解为标识第一个调用者的唯一key，然后再给它对应的值保证原子性的+1
 * （AtomicPositiveInteger是原子的），再对这个值取模总权重，再每次对其权重值-1，知道它取模与总权重值等于0就选择该调用者，
 * 可以称之为"降权取模"（只是一种的计算层面,而不是真正降权）。
 *
 * 总结：轮询调用并不是简单的一个接着一个依次调用，它是根据权重的值进行循环的
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {
    public static final String NAME = "roundrobin";
    private final ConcurrentMap<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {

        String key = invokers.get(0).getUrl().getServiceKey() + "." + invocation.getMethodName();
        int length = invokers.size(); // Number of invokers
        int maxWeight = 0; // The maximum weight
        int minWeight = Integer.MAX_VALUE; // The minimum weight
        final LinkedHashMap<Invoker<T>, IntegerWrapper> invokerToWeightMap = new LinkedHashMap<Invoker<T>, IntegerWrapper>();
        int weightSum = 0;
        //初始化maxWeight，minWeight，weightSum，invokerToWeightMap
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            maxWeight = Math.max(maxWeight, weight); // Choose the maximum weight
            minWeight = Math.min(minWeight, weight); // Choose the minimum weight
            if (weight > 0) {
                invokerToWeightMap.put(invokers.get(i), new IntegerWrapper(weight));
                weightSum += weight;
            }
        }
        // 获取自增调用次数
        AtomicPositiveInteger sequence = sequences.get(key);
        if (sequence == null) {
            sequences.putIfAbsent(key, new AtomicPositiveInteger());
            sequence = sequences.get(key);
        }
        // 个人理解为当前调用总次数
        int currentSequence = sequence.getAndIncrement();
        //当权重不一样的时候，通过加权轮询获取到invoker,权值越大，则被选中的几率也越大
        if (maxWeight > 0 && minWeight < maxWeight) {
            int mod = currentSequence % weightSum;
            for (int i = 0; i < maxWeight; i++) {
                //遍历invoker的数量
                for (Map.Entry<Invoker<T>, IntegerWrapper> each : invokerToWeightMap.entrySet()) {
                    final Invoker<T> k = each.getKey();
                    //invoker的权重
                    final IntegerWrapper v = each.getValue();
                    if (mod == 0 && v.getValue() > 0) {
                        return k;
                    }
                    if (v.getValue() > 0) {
                        //当前invoker的可调用次数减1
                        v.decrement();
                        mod--;
                    }
                }
            }
        }
        // Round robin 权重一样的情况下，就取余的方式获取到invoker
        return invokers.get(currentSequence % length);
    }

    private static final class IntegerWrapper {
        private int value;

        public IntegerWrapper(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public void decrement() {
            this.value--;
        }
    }
}
