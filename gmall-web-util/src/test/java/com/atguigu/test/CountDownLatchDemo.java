package com.atguigu.test;

import java.util.concurrent.CountDownLatch;

/**
 * 一共6人，需求6人全部出了教室，班长再关门
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t离开教室");
                countDownLatch.countDown();// 计数器，每次减少一个
            },String.valueOf(i)).start();
        }
        countDownLatch.await();
        System.out.println("班长开始锁门");
    }
}
