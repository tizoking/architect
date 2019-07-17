package club.maddm;

/**
 * 优先级
 */
public class TestThread006 {
    public static void main(String[] args) {
        System.out.println("main.....开始执行");

        //执行线程
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 60; i++) {
                System.out.println(Thread.currentThread() + "....i : " + i);
            }
        });

        //执行线程
        thread.start();
        //设计优先级
        thread.setPriority(10);


        for (int i = 0; i < 30; i++) {
            System.out.println("主线程....i : " + i);
        }
        System.out.println("main.....执行结束");
    }
}
