# 前言：

&emsp;&emsp;Disruptor呢是一个比较冷门的并发框架，属于第三方框架，中国没有文档，可以在[并发编程网](http://ifeve.com/)查看译文。



# 一、什么是Disruptor

&emsp;&emsp;Disruptor它是一个开源的并发框架，并获得2011 Duke’s 程序框架创新奖，能够在无锁的情况下实现网络的Queue并发操作。

&emsp;&emsp;Disruptor是一个高性能的异步处理框架，或者可以认为是最快的消息框架（轻量的JMS），也可以认为是一个观察者模式的实现，或者**事件监听模式**的实现。

&emsp;&emsp;底层使用的`CAS`**无锁机制**，要比并发包中的阻塞队列要快很多。其是一个高性能队列，基于事件驱动器。



![1565082162910](img\1565082162910.png)



# 二、使用Disruptor

### 2.1 实现生产者消费者案例

#### 2.1.1 maven依赖

```xml
<dependencies>
    <dependency>
        <groupId>com.lmax</groupId>
        <artifactId>disruptor</artifactId>
        <version>3.2.1</version><!-- 建议使用3.0以上版本 -->
    </dependency>
</dependencies>
```

#### 2.1.2 Event消费数据【所谓的事件】

```java
/**
 * 定义事件event【通过Disruptor 进行交换的数据】
 */
public class LongEvent{

	private Long value;

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
}
```

#### 2.1.3 EventFactory事件工厂

```java
/**
 * 事件工厂
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
```

#### 2.1.4 消费者

```java
/**
 * 消费者
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    /**
     * 实现接口后，就可以接受推送过来的信息
     * @param longEvent
     * @param l
     * @param b
     * @throws Exception
     */
    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        System.out.println("【消费者】：longEvent" + longEvent.getValue());
    }
}
```

#### 2.1.5 生产者

```java
public class LongEventProducer {
    /**
     * 环形容器
     */
    private RingBuffer<LongEvent> ringBuffer;

    /**
     * 构造
     * @param ringBuffer 环形容器
     */
    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * byteBuffer:Nio中的内容
     * @param byteBuffer
     */
    public void onData(ByteBuffer byteBuffer) {
        //获取事件队列下标位置
        long sequeueIndex = ringBuffer.next();
        try {
            //取到事件【空的Event】
            LongEvent event = ringBuffer.get(sequeueIndex);
            //放入数据,从传入的这个参数中拿出来的
            event.setValue(byteBuffer.getLong(0));
        } catch (Exception e) {

        }finally {
            System.out.println("【生产者】生产数据成功..");
            //发送数据
            ringBuffer.publish(sequeueIndex);
        }
    }

}
```

#### 2.1.6 完成操作

```java
public class Main {
    public static void main(String[] args) {
        //1.创建一个可以缓存的线程池，提供发给consumer
        ExecutorService executor = Executors.newCachedThreadPool();
        //2.创建工厂
        EventFactory longEventFactory = new LongEventFactory();
        //3.创建一个ringbuffer大小
        int ringbuffer = 1024 * 1024;//2的N次方
        //4.创建disruptor
        Disruptor<LongEvent> disruptor =
                new Disruptor<LongEvent>(longEventFactory, ringbuffer, executor,
                        ProducerType.MULTI, new YieldingWaitStrategy());

        //5.连接消费者。【注册消费者】,像这个消费者投递。
        // 连接多个消费者是重复消费，想要分摊需要分组
        disruptor.handleEventsWith(new LongEventHandler());
        //6.启动
        disruptor.start();

        //7.拿到RingBuffer环形容器
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        //8.创建生产者
        LongEventProducer producer = new LongEventProducer(ringBuffer);
        //9.创建指定大小的缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        for (int i = 1; i < 100; i++) {
            //出入数据
            byteBuffer.putLong(0, i);
            producer.onData(byteBuffer);
        }
        //关闭线程池
        executor.shutdown();
        //关闭disruptor框架
        disruptor.shutdown();
    }
}
```

