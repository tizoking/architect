package club.maddm;

public class ThreadSafety_ThreadLocal implements Runnable {
    private Res res;

    public ThreadSafety_ThreadLocal(Res res) {
        this.res = res;
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            //生成序列号
            System.out.println(Thread.currentThread().getName() + ":" + res.getNumber());
        }
    }

    public static void main(String[] args) {
        Res res = new Res();
        ThreadSafety_ThreadLocal threadLocal = new ThreadSafety_ThreadLocal(res);

        new Thread(threadLocal, "t1").start();
        new Thread(threadLocal, "t2").start();
    }
}

class Res {
    /**
     * 使用threadLocal
     */
    public static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };

    //生成序列号
    public Integer getNumber() {
        int count = threadLocal.get() + 1;
        threadLocal.set(count);
        return count;
    }
}