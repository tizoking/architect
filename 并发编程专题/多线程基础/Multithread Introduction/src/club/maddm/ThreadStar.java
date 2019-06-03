package club.maddm;

//在一个进程中，一定会有哪个线程？----主线程
//线程的几种分类：用户线程、守护线程
//主线程、子线程、Gc线程


//1. 继承Thread类
//2. 实现runable接口
//3.使用匿名内部类方式
//4. 使用线程池进行管理
public class ThreadStar {
    public static void main(String[] args) {
        System.out.println("main...主线程开始");
        //1. 创建线程
        MyThread myThread = new MyThread();
        //2. 启动线程
        myThread.start();

        for (int i = 0; i < 12; i++) {
            System.out.println("main...i : " + i);
        }
        System.out.println("main...主线程结束");
    }
}

class MyThread extends Thread {
    //1. 继承Thread类
    @Override
    public void run() {
        //run方法中，写线程需要执行的代码
        for (int i = 0; i < 10; i++) {
            System.out.println("mythread...i : " + i);
        }
    }
}