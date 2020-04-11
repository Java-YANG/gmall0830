package com.atguigu.test;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        //Collection collection = null;
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
        // 异常类型
        /*System.out.println(blockingQueue.add("a"));
        System.out.println(blockingQueue.add("b"));
        System.out.println(blockingQueue.add("c"));
        //System.out.println(blockingQueue.add("x"));

        System.out.println("===========================");
        System.out.println(blockingQueue.remove("a"));
        System.out.println(blockingQueue.remove("b"));
        //System.out.println(blockingQueue.remove("c"));

        System.out.println("***************************");
        System.out.println(blockingQueue.element());*/

        // 特殊值
        /*System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("b"));
        System.out.println(blockingQueue.offer("c"));
        //System.out.println(blockingQueue.offer("x"));
        System.out.println("==========================");
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        //System.out.println(blockingQueue.poll());
        //ystem.out.println(blockingQueue.poll());

        System.out.println("**************************");
        System.out.println(blockingQueue.peek());*/


        // 阻塞
        /*blockingQueue.put("a");
        blockingQueue.put("b");
        blockingQueue.put("c");

        blockingQueue.take();
        blockingQueue.put("x");*/

        // 超时
        blockingQueue.put("a");
        blockingQueue.put("b");
        blockingQueue.put("c");

        blockingQueue.offer("x",3L,TimeUnit.SECONDS);

    }

}
