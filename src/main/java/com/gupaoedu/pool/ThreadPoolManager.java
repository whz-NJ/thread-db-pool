package com.gupaoedu.pool;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author whz
 * @create 2018-07-03 14:36
 * @desc ww
 **/
public class ThreadPoolManager implements ThreadPool
{
    private static int workerNum = 5;
    WorkThread[] workThreads;
    private static volatile int executeTaskNumber = 0;
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
    private static ThreadPoolManager threadPool;
    private AtomicLong threadNum = new AtomicLong(0);

    private ThreadPoolManager() {
        this(workerNum);
    }
    private ThreadPoolManager(int workerNum2)
    {
        if(workerNum2 > 0) {
            this.workerNum = workerNum2;
        }
        workThreads = new WorkThread[workerNum];
        for(int i = 0; i < workerNum; i++)
        {
            workThreads[i] = new WorkThread();
            workThreads[i].setName("ThreadPool-worker" + threadNum.incrementAndGet());
            workThreads[i].start();
        }
    }
    private class WorkThread extends Thread {
        private boolean isRunning = true;

        @Override public void run()
        {
            Runnable car = null;
            while(isRunning)
            {
                //同步锁
                synchronized (taskQueue) {
                    while(isRunning && taskQueue.isEmpty())
                    {
                        try
                        {
                            taskQueue.wait(20);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if(!taskQueue.isEmpty())
                    {
                        try{
                            car = taskQueue.take();
                        }
                        catch(Exception e)
                        {

                        }
                    }
                    if(car != null)
                    {
                        car.run();
                    }
                    executeTaskNumber++;
                    car = null;
                }
            }
        }
        public void stopWorker() {
            this.isRunning = false;
        }
    }

    @Override public void execute(Runnable task)
    {
        synchronized (taskQueue) {
            try{
                taskQueue.put(task);
            }
            catch(Exception e)
            {

            }
            taskQueue.notifyAll();;
        }
    }

    @Override
    public void execute(Runnable[] tasks)
    {
        synchronized (taskQueue)
        {
            for(Runnable task: tasks)
            {
                try {
                    taskQueue.put(task);
                }
                catch(Exception e)
                {

                }
            }
            taskQueue.notifyAll();
        }
    }

    @Override public void execute(List<Runnable> tasks)
    {
        synchronized (taskQueue)
        {
            for(Runnable task: tasks)
            {
                try {
                    taskQueue.put(task);
                }
                catch(Exception e)
                {

                }
            }
            taskQueue.notifyAll();
        }
    }

    @Override public int getExecuteTaskNumber()
    {
        return 0;
    }

    @Override public int getWaitTaskNumber()
    {
        return 0;
    }

    @Override public int getWorkThreadNumber()
    {
        return 0;
    }

    @Override public void destroy()
    {
        while(!taskQueue.isEmpty()) {
            try{
                Thread.sleep(10);
            }
            catch(Exception e)
            {

            }
        }
        for(int i = 0; i < workerNum; i++)
        {
            workThreads[i].stopWorker();
            workThreads[i] = null;
        }
    }
    public static ThreadPool getThreadPool(int workerNum) {
        if(workerNum <= 0)
        {
            workerNum = ThreadPoolManager.workerNum;
        }
        if(threadPool == null)
        {
            threadPool = new ThreadPoolManager(workerNum);
        }
        return threadPool;
    }
}