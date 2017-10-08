package com.learn.Dlockdemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//single Jvm, 多线程有锁订单
public class JvmLockOrder {
    public static void main(String[] args) {

        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newCachedThreadPool();
        final Lock lock = new ReentrantLock();

        for (int i = 0; i < 10; i++) {
            executor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                latch.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            lock.lock();
                            System.out.println("OrderNo:" + getOrdersNo() + " " + Thread.currentThread().getName());
                            lock.unlock();
                        }
                    }
            );
        }
        latch.countDown();
        executor.shutdown();
    }

    static int num = 0;

    public static String getOrdersNo() {
        SimpleDateFormat date = new SimpleDateFormat("YYYYMMDDHHMMSS");
        return date.format(new Date()) + num++;
    }
}

