package com.flash.pool.util;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 第五版
 * 1. 开始的时候和上一版一样，当 workCount < corePoolSize 时，通过创建新的 Worker 来执行任务。
 * 2. 当 workCount = corePoolSize 就停止创建新线程，把任务直接丢到队列里。
 * 3. 但当队列已满且仍然 workCount < maximumPoolSize 时，不再直接走拒绝策略，而是创建非核心线程，直到 workCount = maximumPoolSize，再走拒绝策略。
 */
public class FlashExecutorV5 implements Executor {

    // 工作线程的数量
    private AtomicInteger workCount = new AtomicInteger(0);
    // 存放工作线程 Worker 的引用
    private final HashSet<Worker> workers = new HashSet<>();

    // 核心线程数量
    private volatile int corePoolSize;
    // 最大线程数
    private volatile int maximumPoolSize;
    // 空闲时间
    private volatile long keepAliveTime;
    // 由调用者提供的阻塞队列，核心线程数满了之后往这里放
    private final BlockingQueue<Runnable> workQueue;
    // 拒绝策略
    private volatile RejectedExecutionHandler handler;
    // 线程工厂
    private volatile ThreadFactory threadFactory;

    public FlashExecutorV5(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler,
            ThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
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
