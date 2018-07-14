package com.gupaoedu.pool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whz
 * @create 2018-07-03 6:30
 * @desc ww
 **/
public class MyDbPool {
    int max;
    long maxWait;
    long idleCount;
    // 技师（数量恒定），正在忙、坐在休息室
    // 数据库连接复用
    //线程安全的，数据库连接具有时效性，队列先进先出的特性很适合这种场景
    //如果用Array，每次都用同一个连接，如果用Queue，取得的是最长没有使用的
    // 客户端 ---数据库连接 ---> 数据库
    // 连接要经常用，不然会超时断开
    //标记、区分连接状态的，一个连接一个时间只能在一个queue保存
    LinkedBlockingQueue<MyJdbcConnection> busy;
    LinkedBlockingQueue<MyJdbcConnection> idle;
    // 已经创建了多少连接
    AtomicInteger activeCount = new AtomicInteger(0);

    public void init(int max, long maxWait, long idleCount)
    {
        this.max = max;
        this.maxWait = maxWait;
        this.idleCount = idleCount;
        this.busy = new LinkedBlockingQueue();
        this.idle = new LinkedBlockingQueue();
    }

    public MyJdbcConnection getResource() throws Exception {
        MyJdbcConnection connection = idle.poll();
        // 1. 取现成的
        if(connection != null)
        {
            busy.offer(connection);
            return connection;
        }
        // 2. 没有可用的空闲连接
        // 创建一个新的连接
        if(activeCount.get() < max)
        {
            // TODO 双重校验（AutomicInteger的功劳）
            if(activeCount.incrementAndGet() <= max) {
                connection = new MyJdbcConnection();
                busy.offer(connection);
                //TODO  防止这个连接没有放进去queue
                return connection;
            }
            else {
                activeCount.decrementAndGet();
            }
        }
        // 3. 肯定会有连接池满了的情况，没有空闲的，也不能创建新的
        // 等待其他线程释放连接
        try {
            // TODO poll 如何实现一个等待的，有数据了马上返回
            connection = idle.poll(this.maxWait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            busy.offer(connection);
            return connection;
        }
        else
        {
            throw new Exception("等待超时");
        }
    }
    public void returnResource(MyJdbcConnection dbConnection)
    {
        // 有效性判断
        if (dbConnection == null)
        {
            return;
        }
        //移除繁忙连接
        boolean removeResult = busy.remove(dbConnection);
        if (removeResult)
        {
            // 控制空闲连接的数量
            if (idleCount < idle.size())
            {
                dbConnection.close();
                activeCount.decrementAndGet();
            }

            boolean success = idle.offer(dbConnection);
            if (!success)
            {
                dbConnection.close();
                activeCount.decrementAndGet();
            }
        }
        else
        {
            // 如果移除失败，代表这个连接不可复用
            //关闭连接
            dbConnection.close();
            activeCount.decrementAndGet();
        }
    }
}