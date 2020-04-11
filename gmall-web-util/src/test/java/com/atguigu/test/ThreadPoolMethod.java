package com.atguigu.test;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolMethod {
    public static void main(String[] args) {
        //ExecutorService threadPool = Executors.newFixedThreadPool(5);// 一池5线程(固定线程)
        //ExecutorService threadPool = Executors.newSingleThreadExecutor();// 一池1线程(单线程)
        ExecutorService threadPool = Executors.newCachedThreadPool();// 一池N线程(根据线程量进行扩容)

        try{
            for (int i = 1; i <= 10; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t办理业务");
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            threadPool.shutdown();
        }
    }

}
