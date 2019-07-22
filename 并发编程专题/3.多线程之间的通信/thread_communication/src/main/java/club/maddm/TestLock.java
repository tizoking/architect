package club.maddm;

import lombok.Data;

import java.security.spec.ECField;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试学习Lock锁
 */
public class TestLock {
    public static void main(String[] args) {
        //用于存放生产出来的东西
        ResLock res = new ResLock();

        Condition condition = res.lock.newCondition();//创建用于等待唤醒的对象
        //创建生产者
        ProThreadLock proThread = new ProThreadLock(res,condition);
        //创建消费者
        ConThreadLock conThread = new ConThreadLock(res,condition);

        new Thread(proThread).start();
        new Thread(conThread).start();
    }
}

@Data
class ResLock {
    private String userSex;
    private String userName;
    //表示当前是否可生产消费。true表示可消费，false表示可生产
    public boolean flag = false;
    Lock lock = new ReentrantLock();
}

/**
 * 生产者
 */
@Data
class ProThreadLock implements Runnable {

    private ResLock res;
    private Condition condition;
    public ProThreadLock(ResLock res,Condition condition) {
        this.res = res;
        this.condition = condition;
    }
    @Override
    public void run() {
        int count = 0;
        while (true) {
            try {//程序出现异常也会释放锁
                //开始上锁
                res.lock.lock();

                if (res.flag) {
                    try {
                        condition.await();//释放当前锁资源
                    } catch (Exception e) {

                    }
                }

                if (count == 0) {
                    res.setUserName("小红");
                    res.setUserSex("女");
                } else {
                    res.setUserName("小军");
                    res.setUserSex("男");
                }
                count = (count + 1) % 2;//让count只有0、1两种可能
                res.flag = true;//表示可消费
                condition.signal();//唤醒
            } catch (Exception e) {

            }finally {
                res.lock.unlock();//释放锁
            }
        }
    }
}

/**
 * 消费者
 */
@Data
class ConThreadLock implements Runnable{

    private ResLock res;

    private Condition condition;//这个也必须使用同一个

    public ConThreadLock(ResLock res,Condition condition) {
        this.res = res;
        this.condition = condition;
    }
    @Override
    public void run() {
        while (true) {
            try {
                res.lock.lock();//上锁

                if (!res.flag) {
                    try {
                        condition.await();//释放当前锁资源
                    } catch (Exception e) {

                    }
                }
                System.out.println(res.getUserName() + "," + res.getUserSex());
                res.flag = false;//表示可生产
                condition.signal();//唤醒
            } catch (Exception e) {
            }finally {
                res.lock.unlock();//释放锁
            }
        }
    }
}