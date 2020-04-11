package com.atguigu.test;

import java.util.concurrent.TimeUnit;

class Resource{

    public static synchronized void sendEmail(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("=============sendEmail");
    }

    public static synchronized void sendSMS(){
        System.out.println("============sendSMS");
    }

    public void hello(){
        System.out.println("===========Hello");
    }
}

/**
 * 1. 同一资源，一个资源对象，A发送邮件，B发送短信，谁先发送
 * 2. 同一资源，两个资源对象，A发送邮件，B发送短信，谁先发送 ---------> 非静态同步方法，同步监视器是this(即资源对象)
 * 3.添加一个普通的Hello(),A发送邮件，B发送短信，谁先发送
 * 4.静态方法，一个资源对象，A发送邮件，B发送短信，谁先发送
 * 5.静态方法，两个资源对象，A发送邮件，B发送短信，谁先发送 ---------> 静态同步方法，同步监视器是类本身(即Class模版)
 *
 */
public class Lock8 {
    public static void main(String[] args) throws InterruptedException {
        Resource resource = new Resource();
        Resource resource1 = new Resource();

        new Thread(() -> {
            resource.sendEmail();
        },"A").start();

        Thread.sleep(100);

        new Thread(() -> {
            //resource.sendSMS();
            resource1.sendSMS();
            //resource.hello();
        },"B").start();
    }
}
