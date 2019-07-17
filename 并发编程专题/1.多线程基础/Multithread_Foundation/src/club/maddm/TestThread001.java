package club.maddm;

/**
 * 继承Thread类实现多线程
 */
public class TestThread001 {
    public static void main(String[] args) {

        System.out.println("main.....开始执行");
        //执行线程
        new MyThreadDemo01().start();

        for (int i = 0; i < 10; i++) {
            System.out.println("main....i : " + i);
        }
        System.out.println("main.....执行结束");
    }
}

class MyThreadDemo01 extends Thread {
    @Override
    public void run() {
        System.out.println("MyThread.....开始执行");
        for (int i = 0; i < 10; i++) {
            System.out.println("MyThread....i : " + i);
        }
        System.out.println("MyThread.....执行结束");
    }
}