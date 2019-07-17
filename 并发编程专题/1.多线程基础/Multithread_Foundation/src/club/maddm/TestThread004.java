package club.maddm;

/**
 * 设置守护线程
 */
public class TestThread004 {
    public static void main(String[] args) {
        System.out.println("main.....开始执行");

        //执行线程
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("匿名内部类实现....i : " + i);
            }
        });
        //设置守护线程
        thread.setDaemon(true);

        //执行线程
        thread.start();

        //创建一个非守护线程并运行【控制在main结束后结束】
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("非守护线程....i : " + i);
            }
        }).start();

        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("main....i : " + i);
        }
        System.out.println("main.....执行结束");
    }
}
