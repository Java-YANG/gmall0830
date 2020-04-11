package com.atguigu.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;

import java.util.List;

@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {

    /**
     *
     * @param invokers Invoker可以理解为客户端的调用者
     * @param url URL就是调用者发起的URL请求链接
     * @param invocation Invocation表示的是调用的具体过程
     * @param <T>
     * @return
     * @throws RpcException
     */
    @Adaptive("loadbalance")
    <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
}
