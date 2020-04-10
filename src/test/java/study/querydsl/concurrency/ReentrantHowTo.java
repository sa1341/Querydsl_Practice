package study.querydsl.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;


public class ReentrantHowTo {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public int getCount() {
        lock.lock();
        try {
            logger.info(Thread.currentThread().getName() + " Count: [{}]", count);
            return count++;
        } finally {
            lock.unlock();
        }
    }
}
