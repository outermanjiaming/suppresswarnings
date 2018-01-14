package com.suppresswarnings.osgi.nn.parellel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class PiEstimateTask extends RecursiveTask<Double> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4056181336131831947L;
	private final long begin;
    private final long end;
    private final long threshold; // 分割任务的临界值

    public PiEstimateTask(long begin, long end, long threshold) {
        this.begin = begin;
        this.end = end;
        this.threshold = threshold;
    }

    @Override
    protected Double compute() {  // 实现 compute 方法
        if (end - begin <= threshold) {  // 临界值之下，不再分割，直接计算

            int sign = 1; // 符号，取 1 或者 -1
            double result = 0.0;
            for (long i = begin; i < end; i++) {
                result += sign / (i * 2.0 + 1);
                sign = -sign;
            }

            return result * 4;
        }

        // 分割任务
        long middle = (begin + end) / 2;
        PiEstimateTask leftTask = new PiEstimateTask(begin, middle, threshold);
        PiEstimateTask rightTask = new PiEstimateTask(middle, end, threshold);

        leftTask.fork();  // 异步执行 leftTask
        rightTask.fork(); // 异步执行 rightTask

        double leftResult = leftTask.join();   // 阻塞，直到 leftTask 执行完毕返回结果
        double rightResult = rightTask.join(); // 阻塞，直到 rightTask 执行完毕返回结果

        return leftResult + rightResult; // 合并结果
    }
    
    public static void main1(String[] args) throws Exception {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
    
        PiEstimateTask task = new PiEstimateTask(0, 1000000000, 100000);
    
        double pi = forkJoinPool.invoke(task); // 阻塞，直到任务执行完毕返回结果
    
        System.out.println("π 的值：" + pi);
        
        forkJoinPool.shutdown(); // 向线程池发送关闭的指令
    }
    
    public static void main(String[] args) throws Exception {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);

        PiEstimateTask task = new PiEstimateTask(0, 1_000_000_000, 10_000_000);
        Future<Double> future = forkJoinPool.submit(task); // 不阻塞
        
        double pi = future.get();
        System.out.println("π 的值：" + pi);
        System.out.println("π 的值：" + task.get());
        System.out.println("future 指向的对象是 task 吗：" + (future == task));
        System.out.println(future);
        System.out.println(task);
        forkJoinPool.shutdown(); // 向线程池发送关闭的指令
    }
}