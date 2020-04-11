package com.atguigu.test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 1. ArrayList集合是线程不安全的
 */
public class NotSafeDemo {
    public static void main(String[] args) {
//        List<String> list = new ArrayList<>(); 线程不安全，在高并发下包java.util.ConcurrentModificatonException
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
        List<String> list = new CopyOnWriteArrayList<>();
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(list);
            },String.valueOf(i)).start();
        }
    }
}
