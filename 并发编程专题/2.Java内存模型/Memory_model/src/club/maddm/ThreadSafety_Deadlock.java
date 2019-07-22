package club.maddm;

/**
 * 死锁问题：此案例可能会出现，
 * 一个线程拿到this锁，等待obj锁。另一个线程拿到obj锁，等待this锁
 *
 * 实际开发环境不建议使用下划线命名
 */
public class ThreadSafety_Deadlock {

    public static void main(String[] args) throws InterruptedException {
        Deadlock p1 = new Deadlock();

        new Thread(p1,"t1").start();

        Thread.sleep(40);//等待一会
        p1.flag = false;//走另外一条路径
        new Thread(p1,"t2").start();

    }
}

class Deadlock implements Runnable {
    //同时多个窗口共享100张火车票
    private static int count = 100;
    //用于同步代码块
    private static Object obj = new Object();

    public boolean flag = true;//用于切换执行线路
    @Override
    public void run() {
        if (flag) {
            while (count > 0) {
                //直接买票
                synchronized (obj){
                    try {
                        Thread.sleep(50);//等待时间比切换长一些，容易测试死锁
                    } catch (Exception e) {

                    }
                    //走这边，先拿到obj锁，后拿到this锁
                    sale();
                }

            }
        }else{
            while (count > 0) {
                //走这边，先拿到this锁，后拿到 obj锁
                sale();
            }
        }
    }

    public  synchronized void sale() {//此处一个obj锁，一个this锁
        synchronized (obj){
            try {
                Thread.sleep(10);//休眠10秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (count > 0) {
                System.out.println(Thread.currentThread().getName() + ",出售一张火车票。剩余：" + (--count) + "张车票");
            }
        }
    }
}
