package club.maddm;

import lombok.Data;

@Data
class Res {
	private String userSex;
	private String userName;
	//表示当前是否可生产消费。true表示可消费，false表示可生产
	public boolean flag = false;
}

/**
 * 生产者与消费者
 *
 * synchronized:此时使用synchronized的可能会出现某个线程连续抢占锁，多次输出一个的情况
 */
public class TestProAndCon{
	public static void main(String[] args) {
		//用于存放生产出来的东西
		Res res = new Res();
		//创建生产者
		ProThread proThread = new ProThread(res);
		//创建消费者
		ConThread conThread = new ConThread(res);

		new Thread(proThread).start();
		new Thread(conThread).start();
	}
}

/**
 * 生产者
 */
@Data
class ProThread implements Runnable {

	private Res res;

	public ProThread(Res res) {
		this.res = res;
	}
	@Override
	public void run() {
		int count = 0;
		while (true) {
			synchronized (res) {
				//如果当前不能生产，那么等待
					try {
						if (res.flag) {
							res.wait();//释放当前锁对象
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				if (count == 0) {
					res.setUserName("小红");
					res.setUserSex("女");
				}else {
					res.setUserName("小军");
					res.setUserSex("男");
				}
				count = (count + 1) % 2;//让count只有0、1两种可能
				res.flag = true;//标记当前可消费
				res.notify();//唤醒这把锁的其他线程
			}
		}
	}
}

/**
 * 消费者
 */
@Data
class ConThread implements Runnable{

	private Res res;

	public ConThread(Res res) {
		this.res = res;
	}
	@Override
	public void run() {
		while (true) {
			synchronized (res) {
					try {
						//如果当前为false，也就是不可消费
						if (!res.flag) {
							res.wait();//释放当前锁对象
						}
						Thread.sleep(1000);//休眠1秒
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				System.out.println(res.getUserName() + "," + res.getUserSex());
				res.flag = false;//表示当前可生产
				res.notify();//唤醒此把锁上的其他线程
			}
		}
	}
}