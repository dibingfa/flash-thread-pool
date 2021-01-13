package com.flash.pool.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

/**
 * 第三版
 * 初始化线程池时，直接启动 corePoolSize 个工作线程 Worker 先跑着。
 * 这些 Worker 就是死循环从队列里取任务然后执行。
 * execute 方法仍然是直接把任务放到队列，但队列满了之后直接抛弃
 */
public class FlashExecutorV3 implements Executor {

    // 由调用者提供的阻塞队列
    private final BlockingQueue<Runnable> workQueue;

    public FlashExecutorV3(int corePoolSize, BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        // 直接创建 corePoolSize 个线程并启动
        for (int i = 0; i < corePoolSize; i++) {
            new Thread(new Worker()).start();
        }
    }

    @Override
    public void execute(Runnable command) {
        // 直接往队列里放，等着被工作线程们抢
        if (!workQueue.offer(command)) {
            // 如果队列满了，直接抛弃
            System.out.println("队列满了，直接抛弃");
        }
    }

    private final class Worker implements Runnable {

        // 死循环从队列里读任务，然后运行任务
        @Override
        public void run() {
            Runnable task;
            while (true) {
                if ((task = getTask()) != null) {
                    task.run();
                }
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
