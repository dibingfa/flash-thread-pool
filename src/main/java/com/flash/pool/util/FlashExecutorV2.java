package com.flash.pool.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

/**
 * 第二版
 * 把这个任务 r 丢到一个 tasks 队列中，然后只启动一个线程，就叫它 Worker 线程吧，不断从 tasks 队列中取任务，执行任务
 */
public class FlashExecutorV2 implements Executor {

    // 由调用者提供的阻塞队列
    private final BlockingQueue<Runnable> workQueue;

    public FlashExecutorV2(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        new Thread(new Worker()).start();
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
