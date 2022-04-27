package com.gzhu;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;


public class Lock {
    private static CuratorFramework client;

    public static CuratorFramework getClient() {
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(3000, 10);

        String connectString = "192.168.10.102:2181,192.168.10.103:2181,192.168.10.104:2181";


        client = CuratorFrameworkFactory.builder().connectString(connectString)
                .sessionTimeoutMs(2000)  // 会话超时时间
                .connectionTimeoutMs(2000) // 连接超时时间
                .retryPolicy(retry).build(); // 重试策略

        client.start();

        return client;
    }

    public static void main(String[] args) {
        InterProcessMutex lock1 = new InterProcessMutex(getClient(),"/gzhu/song");

        InterProcessMutex lock2 = new InterProcessMutex(getClient(),"/gzhu/song");

        new Thread(()->{
            try {
                lock1.acquire();
                System.out.println("线程1获得锁");

                lock1.acquire();
                System.out.println("线程1再次获得锁");

                Thread.sleep(5000);

                lock1.release();
                System.out.println("线程1 释放锁");

                lock1.release();
                System.out.println("线程1 再次释放锁");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

        new Thread(()->{
            try {
                lock2.acquire();
                System.out.println("线程2获得锁");

                lock2.acquire();
                System.out.println("线程2再次获得锁");

                Thread.sleep(5000);

                lock2.release();
                System.out.println("线程2 释放锁");

                lock2.release();
                System.out.println("线程2 再次释放锁");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}
