package com.colaorange.dailymoney.core.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dennis
 */
public class Threads {

    private static ExecutorService singleExecutor = Executors.newFixedThreadPool(2);

    public static void execute(Runnable task){
        singleExecutor.submit(task);
    }
}
