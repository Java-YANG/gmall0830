package com.atguigu.test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 资源类
class Print{
    private int number = 1;// 1表示A线程,2表示B线程，3表示C线程
    private Lock lock = new ReentrantLock();
    private Condition condition1 = lock.newCondition();
    private Condition condition2 = lock.newCondition();
    private Condition condition3 = lock.newCondition();

    public void print5(){
        lock.lock();
        // 判断
        try {
            while(number != 1){
                condition1.await();
            }
            System.out.println(Thread.currentThread().getName() + "\t" + "a");
            // 通知
            number = 2;
            condition2.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void print10(){
        lock.lock();
        // 判断
        try {
            while(number != 2){
                condition2.await();
            }
            // 干活

            System.out.println(Thread.currentThread().getName() + "\t" + "b");

            // 通知
            number = 3;
            condition3.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void print15(){
        lock.lock();
        // 判断
        try {
            while(number != 3){
                condition3.await();
            }
            // 干活

            System.out.println(Thread.currentThread().getName() + "\t" + "c");

            // 通知
            number = 1;
            condition1.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}


public class ThreadOrderAccess {
    public static void main(String[] args) {
        Print print = new Print();
        new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                print.print5();
            }
        },"1").start();
        new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                print.print10();
            }
        },"2").start();
        new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                print.print15();
            }
        },"3").start();
    }

}
