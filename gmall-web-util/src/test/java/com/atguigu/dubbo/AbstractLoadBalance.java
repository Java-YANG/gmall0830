package com.atguigu.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

import java.util.List;

/**
 * TIPS : 为什么要预热
 * 答：provider刚启动时的字节码肯定不是最优的，JVM需要对字节码进行优化。预热保用了调用的体验，谨防由此引发的调用超时问题。
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    /**
     * AbstractLoadBlance抽象类继承自LoadBalance，其中有个static方法表明它在类加载的时候就会运行，
     * 它表示的含义是计算预热加载权重，参数是uptime，这里可以理解为服务启动的时间，warmup就是预热时间，weight是权重的值，
     * @param uptime 服务启动的时间
     * @param warmup 预热时间
     * @param weight 权重的值
     * @return
     */
    static int calculateWarmupWeight(int uptime, int warmup, int weight){
        int ww = (int)((float) uptime/((float)warmup/(float)weight));
        return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }

    /**
     *  抽象类方法中有个有方法体的方法select,先判断调用者组成的List是否为null，如果是null就返回null。再判断调用者的大小，
     *  如果只有一个就返回那个唯一的调用者(试想，如果服务调用另一个服务时，当服务的提供者机器只有一个，那么就可以返回那一个，
     *  因为没有选择了！)如果这些都不成立，就继续往下走，走doSelect方法
     * @param invokers Invoker可以理解为客户端的调用者
     * @param url URL就是调用者发起的URL请求链接
     * @param invocation Invocation表示的是调用的具体过程
     * @param <T>
     * @return
     * @throws RpcException
     */
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if(invokers == null || invokers.isEmpty())
            return null;
        if(invokers.size() == 1)
            return invokers.get(0);
        return doSelect(invokers,url,invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation);

    /**
     * 顾名思义，这个方法的含义就是获取权重，首先通过URL(URL为dubbo封装的一个实体)获取基本的权重，如果权重大于0，
     * 会获取服务启动时间，再用当前的时间-启动时间就是服务到目前为止运行了多久，因此这个upTime就可以理解为服务启动时间，
     * 再获取配置的预热时间，如果启动时间小于预热时间，就会再次调用获取权重。这个预热的方法其实dubbo针对JVM做出的一个很契合的优化，
     * 因为JVM从启动到起来都运行到最佳状态是需要一点时间的，这个时间叫做warmup,而dubbo就会对这个时间进行设定，
     * 然后等到服务运行时间和warmup相等时再计算权重，这样就可以保障服务的最佳运行状态！
     * @param invoker
     * @param invocation
     * @return
     */
    protected int getWeight(Invoker<?> invoker, Invocation invocation){
        // 获取provider的权重值
        int weight = invoker.getUrl().getMethodParameter(
                // 调用的方法
                invocation.getMethodName(),
                // 权重KEY值
                Constants.WEIGHT_KEY,
                // 默认权重值
                Constants.DEFAULT_WEIGHT);
        // 判断权重值
        if(weight > 0){
            // provider的启动时间戳
            long timestamp = invoker.getUrl().getParameter(Constants.REMOTE_TIMESTAMP_KEY,0L);
            if(timestamp > 0L){
                // 计算启动时长
                int uptime = (int)(System.currentTimeMillis() - timestamp);
                // 获取预热时长
                int warmup = invoker.getUrl().getParameter(Constants.WARMUP_KEY,Constants.DEFAULT_WARMUP);
                // 如果启动时长少于预热时长，默认是10min，则重新计算权重
                if(uptime > 0 && uptime < warmup){
                    weight = calculateWarmupWeight(uptime,warmup,weight);
                }
            }
        }
        return weight;
    }
}
