package com.flash.pool.util;

import java.util.concurrent.Executor;

/**
 * 第一版
 * 最简单的线程池工具类
 */
public class FlashExecutorV1 implements Executor {

    @Override
    public void execute(Runnable r) {
        new Thread(r).start();
    }

}
