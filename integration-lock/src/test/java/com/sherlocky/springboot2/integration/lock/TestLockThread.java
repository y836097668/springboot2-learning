package com.sherlocky.springboot2.integration.lock;

import com.alibaba.fastjson.JSON;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author: zhangcx
 * @date: 2019/3/27 10:40
 */
public class TestLockThread implements Runnable {
    RedisLockRegistry redisLockRegistry;

    public TestLockThread(RedisLockRegistry redisLockRegistry) {
        this.redisLockRegistry = redisLockRegistry;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "_1_$$$$$$$$$$$$$$$" + System.currentTimeMillis() + "$$$$$$$$$$$$$$$");
        System.out.println(Thread.currentThread().getName() + "_2_###############" + System.currentTimeMillis() + "##############");
        Lock lock = redisLockRegistry.obtain(LockConstants.LOCK_KEY);
        System.out.println(Thread.currentThread().getName() + "_3_###############" + System.currentTimeMillis() + "##############");
        boolean b1 = false;
        try {
            b1 = lock.tryLock(3, TimeUnit.SECONDS);
            System.out.println(Thread.currentThread().getName() + "_4_###############" + System.currentTimeMillis() + "##############");
            info(Thread.currentThread().getName() + " b1 is : {}", b1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("获取锁失败");
        }
        try {
            // do somethings
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 拿到了锁
        if (b1) {
            // 一定要先获得锁的前提下,才能解锁
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + "_5_释放锁#########" + System.currentTimeMillis() + "##############");
        } else {
            System.out.println(Thread.currentThread().getName() + "_5_没有拿到锁#####" + System.currentTimeMillis() + "##############");
        }
        System.out.println(Thread.currentThread().getName() + "_6_###############" + System.currentTimeMillis() + "##############");
    }

    private void info(String msg, Object obj) {
        System.out.println(msg + JSON.toJSONString(obj));
    }

}
