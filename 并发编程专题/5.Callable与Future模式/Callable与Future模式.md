# 前言：

#### 需要掌握的知识点：

- Callable接口
- Future的常用方法
- Future的运行模式



# 一、Callable接口

### 1.1 引出案例

&emsp;&emsp;需求：使用多线程技术，实现多线程下载，每个线程都会发送Http请求资源，进行下载操作，会有下载进度。



### 1.2 多线程的返回值

&emsp;&emsp;实现`Callable`接口就可以完成一个带有返回值的线程，结合`Future`模式就能很好的完成异步操作。



# 二、Future

&emsp;&emsp;将我们的`Callable`线程放入线程池运行后，返回一个`Future`对象，这个对象该如何使用呢。

### 2.1 Future的常用方法

| 方法名                                        | 描述                                                         |
| --------------------------------------------- | ------------------------------------------------------------ |
| `V get()`                                     | 获取异步执行结果，如果没有结果可用，阻塞到异步计算完成       |
| `V get(Long timeout,TimeUnit unit)`           | 获取异步执行结果，如果没有结果可用，阻塞一定时间，抛异常     |
| `boolean isDone()`                            | 程序是否执行结束，异常和中途取消都是已经结束了               |
| `boolean isCanceller()`                       | 任务完成前是否被取消                                         |
| `boolean cancel(boolean mayInterruptRunning)` | 打断线程的执行，传入`true`为尝试打断，任务完成或<br />未运行返回`false`，打断成功返回`true`。 |



### 2.2 Future运行模式

&emsp;&emsp;底层通过这种形式去执行任务，保证了主线程可以异步的获取到结果。



`Data`接口：

```java
//公共的data数据结果
public interface Data {
    /**
     * 返回线程执行结果
     * @return
     */
    String getRequest();
}

```



`FutureData`类：

```java
public class FutureData implements Data {
    //读取结果
    private boolean flag = false;
    private RealData realData;
    /**
     * 读取Data书库
     */
    public synchronized void setRealData(RealData realData) {
        //如果获取到将结果，直接返回
        if (flag) {
            return;
        }
        //如果flag为false，没有获取到数据,传递realData对象
        this.realData = realData;
        flag = true;
        notify();//唤醒
    }

    @Override
    public synchronized String getRequest() {
        while (!flag) {
            //false就一直等待
            try {
                wait();
            } catch (Exception e) {

            }
        }
        return realData.getRequest();
    }
}
```



`RealData`真实数据：

```java
public class RealData implements Data {

    private String requestResult;

    public RealData(String requestData) {
        System.out.println("正在使用data进行网络请求，data：" + requestData+ ",开始。。。");
        try {
            //业务耗时
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("操作执行完毕。。。获取到结果");
        //获取返回结果
        this.requestResult = "返回值结果";
    }

    @Override
    public String getRequest() {
        return requestResult;
    }
}
```



`FutureClient`类：

```java
public class FutureClient {
    //
    public Data submit(String requestData) {
        FutureData futureData = new FutureData();

        /**
         * 这个线程没有执行完毕时，get方法会阻塞
         */
        new Thread(() -> {
            //这段代码会阻塞
            RealData realData = new RealData(requestData);
            futureData.setRealData(realData);
        }).start();

        return futureData;
    }

}
```



主线程：

```java
public class Main {
    public static void main(String[] args) {
        FutureClient client = new FutureClient();
        Data request = client.submit("123321");
        System.out.println("main：数据发送成功");
        System.out.println("主线程执行其他任务");
        String result = request.getRequest();
        System.out.println("主线程获取到结果：" + result);
    }
}
```



