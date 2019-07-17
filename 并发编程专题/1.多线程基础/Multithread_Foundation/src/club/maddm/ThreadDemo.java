package club.maddm;

/**
 * 练习：现在有T1、T2、T3三个线程，你怎样保证T2在T1执行完后执行，T3在T2执行完后执行
 *
 * 答：使用join方法
 */
public class ThreadDemo {
    public static void main(String[] args) {


        final Thread t1 = new Thread(() -> {
            //线程：T1
            for (int i = 0; i < 20; i++) {
                System.out.println("我是线程T1");
            }
        });

        final Thread t2 = new Thread(() -> {

            try {
                //等待线程t1执行完毕
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程：T2
            for (int i = 0; i < 20; i++) {
                System.out.println("我是线程T2");
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                //等待线程t1执行完毕
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //线程：T3
            for (int i = 0; i < 20; i++) {
                System.out.println("我是线程T3");
            }
        });

        t1.start();
        t2.start();
        t3.start();

    }
}
