package io.renren.threadpool.p2;


/**
 * Created by pdy18 on 2019/12/20.
 */
public class TestDemo {
    public static void main(String[] args){
        FixedSizeThreadPool pool = new FixedSizeThreadPool(3,6);
        for (int i = 0; i < 6; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("一个任务放入！");
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        //doSomeThing 报错降级？60s-->5 超过5次，
                    }
                }
            });
        }
        pool.shutDown();
    }
}
