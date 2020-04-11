package com.atguigu.test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3); // 模拟资源类，3个车位

        for (int i = 1; i <= 6; i++) {// 6个车主抢三个车位
            new Thread(() -> {
                try {
                    semaphore.acquire();// 获取信号量，信号量减1
                    System.out.println(Thread.currentThread().getName() + "\t抢到了车位");
                    TimeUnit.SECONDS.sleep(3);
                    System.out.println(Thread.currentThread().getName() + "\t离开了车位");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();// 释放信号量，信号量加1
                }
            },String.valueOf(i)).start();
        }
    }
}
