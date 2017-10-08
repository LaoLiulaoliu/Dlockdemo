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

/* 分布锁：
缓存：（不可重入，非阻塞，memcache不支持持久化）
    redis setnx
    memcache add
Zookeeper: (可重入，安全性高)
Java客户端：zkclient, curator

原理：zookeeper分布式协调开放服务框架
like, dubbo jstorm disconf
分布式锁：
节点：znode四种节点树形结构
persistent(0, false, false);
persistent_sequential(2, false, true);
ephemeral(1, true, false); 退出用户Session则创建的节点消失
ephemeral_sequential(3, true, true);

Wathcer:
abstract public void process(WatchedEvent event);
KeeperState 通知状态
EventType 通知类型

ZAB协议:
zk的原子消息广播协议，paxos算法改进而来
Zab有全局zxId 全局的唯一性

 */
