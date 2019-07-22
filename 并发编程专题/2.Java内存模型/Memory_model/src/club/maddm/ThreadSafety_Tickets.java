package club.maddm;

/**
 * 线程安全问题：火车票例子。
 * 实际开发环境不建议使用下划线命名
 */
public class ThreadSafety_Tickets {

    public static void main(String[] args) throws InterruptedException {
        TicketPeopro p1 = new TicketPeopro();
//        TicketPeopro p2 = new TicketPeopro();

        new Thread(p1,"窗口1").start();

        Thread.sleep(40);//休眠一会
        p1.flag = false;//走另外一条路径
        new Thread(p1,"窗口2").start();
//        new Thread(p2,"窗口3").start();
    }
}

class TicketPeopro implements Runnable {
    //同时多个窗口共享100张火车票
    private static int count = 100;
    //用于同步代码块
    private static Object obj = new Object();

    public boolean flag = true;//用于切换执行线路
    @Override
    public void run() {
        if (!flag) {//取非，让同步方法运行，然后直接休眠大量时间。在换成同步代码块方式，如果同步代码块方式能正常执行，那么说明不是一个锁
            while (count > 0) {
                /*try {
                    //为了更好的模拟出线程安全问题
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                //直接买票
                synchronized (/*TicketPeopro.class*/this.getClass()){//将代码块换成this锁，
                    try {
                        //为了更好的模拟出线程安全问题
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (count > 0) {
                        System.out.println(Thread.currentThread().getName() + ",出售一张火车票。剩余：" + (--count) + "张车票");
                    }
                }

            }
        }else{
            while (count > 0) {
                try {
                    //为了更好的模拟出线程安全问题
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //买票，使用同步方法
                sale();
            }
        }
    }

    public static synchronized void sale() {
        try {
            Thread.sleep(10000);//休眠10秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        synchronized (obj){
            if (count > 0) {
                System.out.println(Thread.currentThread().getName() + ",出售一张火车票。剩余：" + (--count) + "张车票");
            }
//        }
    }
}
