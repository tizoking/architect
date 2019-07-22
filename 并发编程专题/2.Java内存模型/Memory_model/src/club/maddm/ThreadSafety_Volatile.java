package club.maddm;

/**
 * volatile演示
 */
public class ThreadSafety_Volatile {
    public static void main(String[] args) throws InterruptedException {
        ThreadDemo threadDemo = new ThreadDemo();
        new Thread(threadDemo).start();
        Thread.sleep(3000);
        threadDemo.setFlag(false);
        System.out.println("flag已经设置为false");
        Thread.sleep(1000);
        System.out.println("flag:" + threadDemo.getFlag() );
    }
}

class ThreadDemo implements Runnable {

    private volatile boolean flag = true;//此时不加volatile线程会一直执行

    @Override
    public void run() {
        System.out.println("线程开始。。。。");
        while (flag) {

        }
        System.out.println("线程结束。。。。");
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
    public boolean getFlag() {
        return this.flag;
    }
}