package club.maddm;

/**
 * join方法
 */
public class TestThread005 {
    public static void main(String[] args) {
        System.out.println("main.....开始执行");

        //执行线程
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 60; i++) {
                System.out.println("子线程....i : " + i);
            }
        });

        //执行线程
        thread.start();
        //让子线程先执行
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 30; i++) {
            System.out.println("主线程....i : " + i);
        }
        System.out.println("main.....执行结束");
    }
}
