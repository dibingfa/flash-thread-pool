package com.flash.pool.util;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 第四版
 * 按需创建Worker
 * 增加拒绝策略
 * 增加线程工厂
 */
public class FlashExecutorV4 implements Executor {

    // 工作线程的数量
    private AtomicInteger workCount = new AtomicInteger(0);
    // 存放工作线程 Worker 的引用
    private final HashSet<Worker> workers = new HashSet<>();

    // 核心线程数量（超过了就放队列里）
    private volatile int corePoolSize;
    // 由调用者提供的阻塞队列，核心线程数满了之后往这里放
    private final BlockingQueue<Runnable> workQueue;
    // 拒绝策略
    private volatile RejectedExecutionHandler handler;
    // 线程工厂
    private volatile ThreadFactory threadFactory;

    public FlashExecutorV4(
            int corePoolSize,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler,
            ThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        this.workQueue = workQueue;
        this.handler = handler;
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        if (workCount.get() < corePoolSize) {
            // 工作线程数 <= 核心线程时，新建工作线程
            Worker w = new Worker(command);
            // 增加工作线程数
            workCount.getAndIncrement();
            workers.add(w);
            // 并且把它启动
            w.thread.start();
        } else {
            // 工作线程数 > 核心线程时，放入队列
            if (!workQueue.offer(command)) {
                // 放入队列失败，走拒绝策略
                handler.rejectedExecution(command, this);
            }
        }
    }

    private final class Worker implements Runnable {

        final Thread thread;
        private Runnable task;

        Worker(Runnable firstTask) {
            this.task = firstTask;
            this.thread = threadFactory.newThread(this);
        }

        // 死循环从队列里读任务，然后运行任务
        @Override
        public void run() {
            while (task != null || (task = getTask()) != null) {
                task.run();
                task = null;
            }
        }

        // 阻塞地从队列里获取一个任务
        private Runnable getTask() {
            try {
                return workQueue.take();
            } catch (InterruptedException e) {
                return null;
            }
        }

    }

}
