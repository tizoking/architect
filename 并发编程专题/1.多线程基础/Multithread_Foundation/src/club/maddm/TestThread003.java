package club.maddm;

/**
 * 使用内部类的方式实现多线程
 */
public class TestThread003 {
    public static void main(String[] args) {
        System.out.println("main.....开始执行");

        //执行线程
        new Thread(()-> {
            for (int i = 0; i < 10; i++) {
                System.out.println("匿名内部类实现....i : " + i);
            }
        }).start();

        for (int i = 0; i < 10; i++) {
            System.out.println("main....i : " + i);
        }
        System.out.println("main.....执行结束");
    }
}
