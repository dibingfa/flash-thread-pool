package com.flash.pool;

import com.flash.pool.util.FlashExecutorV3;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ArrayBlockingQueue;

@SpringBootApplication
public class FlashThreadPoolApplication {

    public static void main(String[] args) throws InterruptedException {
        //SpringApplication.run(FlashThreadPoolApplication.class, args);
        FlashExecutorV3 flashExecutor = new FlashExecutorV3(2, new ArrayBlockingQueue<>(3));
        flashExecutor.execute(new FlashTask("t1"));
        flashExecutor.execute(new FlashTask("t2"));
        flashExecutor.execute(new FlashTask("t3"));
        flashExecutor.execute(new FlashTask("t4"));
        flashExecutor.execute(new FlashTask("t5"));
        flashExecutor.execute(new FlashTask("t6"));
        flashExecutor.execute(new FlashTask("t7"));
        flashExecutor.execute(new FlashTask("t8"));
        flashExecutor.execute(new FlashTask("t9"));
    }

    private static class FlashTask implements Runnable {

        private String name;

        public FlashTask(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println(name);
        }
    }

}
