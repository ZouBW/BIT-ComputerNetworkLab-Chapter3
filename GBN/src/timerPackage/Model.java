package timerPackage;

/**
 * 保证线程安全性
 */
public class Model {

    public volatile int time;

    public  synchronized int getTime() {
        return time;
    }

    public synchronized void setTime(int time) {
        this.time = time;
    }
}

