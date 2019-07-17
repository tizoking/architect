package club.maddm;

/**
 * 实现Runnable接口实现多线程
 */
public class TestThread002 {
    public static void main(String[] args) {
        System.out.println("main.....开始执行");
        //执行线程
        new Thread(new MyThreadDemo02()).start();

        for (int i = 0; i < 10; i++) {
            System.out.println("main....i : " + i);
        }
        System.out.println("main.....执行结束");
    }
}

class MyThreadDemo02 implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("MyThreadDemo02....i : " + i);
        }
    }
}
