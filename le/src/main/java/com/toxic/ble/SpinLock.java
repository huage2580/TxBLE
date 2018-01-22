package com.toxic.ble;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 不可重入IO锁
 * Created by hua on 2017/11/13.
 */

public class SpinLock {
    private ArrayBlockingQueue queue = new ArrayBlockingQueue(1);
    private Object signObj=new Object();

    /**
     * 阻塞式获取锁，不可重入,10s超时
     * @return false 写入失败，没有释放锁
     */
    public boolean lock(){
        boolean result=false;
        try {
          //  System.out.println("lock"+System.currentTimeMillis());
            result = queue.offer(signObj,10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            return result;
        }
    }

    /**
     * 释放锁
     */
    public void unlock(){
        try {
            if (queue.size()==0){
                return;
            }
           // System.out.println("unlock"+System.currentTimeMillis());
            queue.poll(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}