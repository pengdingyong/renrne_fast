package io.renren.threadpool.p2;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by pdy18 on 2019/12/20.
 */
public class FixedSizeThreadPool {
    private static FixedSizeThreadPool ourInstance = new FixedSizeThreadPool();

    public static FixedSizeThreadPool getInstance() {
        return ourInstance;
    }

    private FixedSizeThreadPool() {
    }
    //1.队列
    private BlockingQueue<Runnable> blockingQueue;

    //2.线程-->集合
    private List<Thread> workers;

    //3.每个线程需要干的活
    public static class Worker extends Thread{

        private FixedSizeThreadPool pool;

        public Worker(FixedSizeThreadPool pool) {
            this.pool = pool;
        }

        @Override
        public void run() {//每个小姐姐要做的事
            //runnable中定义的代码块--》
            while (this.pool.isWorking || this.pool.blockingQueue.size()>0) {
                Runnable task = null;
                try {
                    if (this.pool.isWorking) {
                        task = this.pool.blockingQueue.take();//如果仓库没有东西，就会阻塞(等待)在这里
                    }else {
                        task = this.pool.blockingQueue.poll();//不阻塞状态这么拿(不用等待了)
//                        boolean offer = this.pool.blockingQueue.offer(task,2000L, TimeUnit.MILLISECONDS);
//                        if (offer){
//                            //doSomeThing
//                        }
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (task != null){
                    task.run();
                    System.out.println("线程："+Thread.currentThread()+"执行完毕");
                }
            }
        }
    }
    public FixedSizeThreadPool(int poolSize,int taskSize) {
        this.blockingQueue = new LinkedBlockingDeque<>(taskSize);
        this.workers = Collections.synchronizedList(new ArrayList<>());//线程安全写法

        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(this);
            workers.add(worker);
            worker.start();
        }
    }

    //引导客人
    public boolean submit(Runnable task){
        if (this.isWorking){
            return this.blockingQueue.offer(task);
        }else {
            return false;
        }
    }

    //关闭方法
    //1.客人停止进入
    //2.当关闭的时候，还有任务怎么办？执行完毕
    //3.当调用关闭的时候，线程去哪任务不应当再阻塞了
    //4.已经阻塞的线程？？
    //volatile关键字：可以使变量变成动态的，每次重新获取，修饰被不同线程访问和修改的变量，
    public volatile boolean isWorking = true;

    public void shutDown(){
        this.isWorking = false;
        for (Thread thread:workers){
            if (thread.getState().equals(Thread.State.BLOCKED)){//判断线程是否阻塞
                thread.interrupt();//中断它
            }
        }
    }
}
