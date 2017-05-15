package zz.demo;

import java.time.Period;
import java.util.concurrent.TimeUnit;

/**
 * Created by zulk on 10.03.17.
 */
public class Bench {
    public static void main(String[] args) throws InterruptedException {
//        whileM();
        forM();
    }

    private static void forM() throws InterruptedException {
        long start = System.currentTimeMillis();
        long max = Integer.MAX_VALUE;
        long cnt = -1;
        for(long i=0;i<max;i++){
             cnt = i;
        };
        long end = System.currentTimeMillis();
        System.out.println(Double.class.cast((end-start)/1000.0)+" "+cnt+" "+max);
    }

    private static void whileM() throws InterruptedException {
        long start = System.currentTimeMillis();
        long i = 0;
        long max = Integer.MAX_VALUE;
        long cnt = -1;
        while(i<max) {
            i++;
            cnt = i;
        }
        long end = System.currentTimeMillis();
        System.out.println(Double.class.cast((end-start)/1000.0)+" "+cnt+" "+max);
    }
}

