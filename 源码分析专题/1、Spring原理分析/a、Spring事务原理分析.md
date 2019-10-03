# 前言：

### 需要掌握的知识点：

- 事务的底层实现原理：基于AOP实现【动态代理】
- Spring的两种类型的事务：编程式事务，声明式事务【Spring底层使用编程式事务包装】
- 掌握spring核心技术:`IOC`和`AOP`【代理 + 工厂】
- 关注点：相同点【重复代码】
- 切面：关注点形成的类，就相当于所有重复代码抽取出来，运行时业务方法上动态植入。
- 切入点：执行目标的对象。
- `jdk`动态代理：反射实现
- `CGLIB`动态代理：字节码技术实现



# 一、Spring事务使用



### 1.1 事务分类

- **编程式事务：**手动事务【begin、commit、rollback】
- **声明式事务：**原理使用编程事务 + 反射机制进行包装、自动事务（注解/Xml）



### 1.2 事务的基本特性

&emsp;&emsp;原子性、一致性、隔离性、持久性





# 二、 手写Spring事务【Aop】



**Mapper:**

```java
@Repository
public class UserMapper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void add(String name, Integer age) {

        String sql = " INSERT INTO user(name,age) VALUES(?,?) ";

        int  updateResult = jdbcTemplate.update(sql, name, age);
        System.out.println("updateResult:" + updateResult);

    }
}
```

**Service:**

```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TransactionUtils transactional;
    
    public void add() {
        //使用声明式事务时：service不要try 最好把异常抛给aop
        userMapper.add("小明",17);
        int i = 1/0;
        System.out.println("#####################");
        userMapper.add("小毛",24);
    }
}
```

**Utils:**

```java
@Component
@Scope("prototype")//不能为单例 可能会有线程安全问题
public class TransactionUtils {

	@Autowired
	private DataSourceTransactionManager dataSourceTransactionManager;//拿到数据源接口

	// 开启事务
	public TransactionStatus begin() {
		TransactionStatus transaction
				= dataSourceTransactionManager
				.getTransaction(new DefaultTransactionAttribute());

		return transaction;
	}
	// 提交事务
	public void commit(TransactionStatus transactionStatus) {
		dataSourceTransactionManager.commit(transactionStatus);
	}

	// 回滚事务
	public void rollback(TransactionStatus transactionStatus) {
		dataSourceTransactionManager.rollback(transactionStatus);
	}
}
```

**Aop:**

```java
//切面类：基于手动事务封装
@Component
@Aspect
public class AopTransactional {
    @Autowired
    private TransactionUtils transactionUtils;

    @AfterThrowing("execution(* club.maddm.service.UserService.add(..))")
    public void afterThrowing() {
        System.out.println("异常通知：回滚事务");
        //获取当前事务 直接回滚
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

    }

    @Around("execution(* club.maddm.service.UserService.add(..))")
    public void around(ProceedingJoinPoint proceedingJoinPoint)throws Throwable {

        System.out.println("开启事务");
        TransactionStatus begin = transactionUtils.begin();

        /*try {*/
            proceedingJoinPoint.proceed();
            System.out.println("提交事务");
            transactionUtils.commit(begin);
        /*} catch (Exception e) {
            System.out.println("回滚事务");
            transactionUtils.rollback(begin);
        }*/
    }
}
```



**涉及到的配置：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">

	<!-- 扫包 -->
	<context:component-scan base-package="club.maddm"></context:component-scan>
	<aop:aspectj-autoproxy></aop:aspectj-autoproxy> <!-- 开启aop/事物注解 -->

	<!-- 1. 数据源对象: C3P0连接池 -->
	<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
		<property name="driverClass" value="com.mysql.jdbc.Driver"></property>
		<property
				name="jdbcUrl"
				value="jdbc:mysql://localhost:33306/meite?characterEncoding=UTF-8&amp;useUnicode=TRUE"></property>
		<property name="user" value="root"></property>
		<property name="password" value="123456"></property>
	</bean>

	<!-- 2. JdbcTemplate工具类实例 -->
	<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
		<property name="dataSource" ref="dataSource"></property>
	</bean>

	<!-- 3.配置事务 -->
	<bean id="dataSourceTransactionManager"
		  class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<property name="dataSource" ref="dataSource"></property>
	</bean>
</beans>
```

**用到的jar包：**

```xml
<dependencies>
  <!-- 引入Spring-AOP等相关Jar -->
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>3.0.6.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>3.0.6.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aop</artifactId>
    <version>3.0.6.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-orm</artifactId>
    <version>3.0.6.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.6.1</version>
  </dependency>
  <dependency>
    <groupId>aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.5.3</version>
  </dependency>

  <dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>2.1_2</version>
  </dependency>
  <dependency>
    <groupId>com.mchange</groupId>
    <artifactId>c3p0</artifactId>
    <version>0.9.5.2</version>
  </dependency>
  <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.37</version>
  </dependency>
</dependencies>
```



# 三、手写注解版Spring事务



**自定义事务注解:**

```java
/**
 * 自定义事务注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtTransactional {
}
```



**Aop切面配置：**

```java
@Component
@Aspect
public class ExtAop {

    @Autowired
    private TransactionUtilsTest transactionUtils;


    @AfterThrowing("execution(* club.maddm.service.*.* (..))")
    public void afterThrowing() {
        rollback();
    }

    @Around("execution(* club.maddm.service.*.* (..))")
    public void around(ProceedingJoinPoint proceedingJoinPoint)throws Throwable {
        //1、获取当前方法上的事务注解
        ExtTransactional annotation = getExtTransactional(proceedingJoinPoint);
        //2、判断是否开启事务
        getTransactionStatus(annotation);
        //3、调用目标方法
        proceedingJoinPoint.proceed();
        //4、如果存在事务，提交事务
        commit();

    }

    /**
     * 存在事务，提交事务
     */
    private void commit() {
        //5、判断方法上是否存在注解提交或回滚事务
        if (transactionUtils.isExistingTransaction()) {
            System.out.println("提交事务");
            transactionUtils.commit();
        }
    }

    /**
     * 根据是否存在事务注解，判断是否开启事务
     * @param annotation
     * @return
     */
    private void getTransactionStatus(ExtTransactional annotation) {
        if (annotation != null) {
            //存在事务开启事务
            System.out.println("开启事务");
            transactionUtils.begin();
        }
    }

    /**
     * 判断方法上是否有事务注解
     * @param proceedingJoinPoint
     * @return
     * @throws NoSuchMethodException
     */
    private ExtTransactional getExtTransactional(ProceedingJoinPoint proceedingJoinPoint) throws NoSuchMethodException {

        //1、获取当前调用方法的名称
        String methodName = proceedingJoinPoint.getSignature().getName();
        //2、获取目标对象
        Class<?> classTarget = proceedingJoinPoint.getTarget().getClass();
        //3、获取目标对象类型
        Class[] types = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterTypes();
        //4、获取目标对象方法
        Method objMethod = classTarget.getMethod(methodName, types);

        //返回当前方法上的事务注解
        return objMethod.getAnnotation(ExtTransactional.class);
    }

    /**
     * 回滚事务
     */
    private void rollback() {
        System.out.println("异常通知：如果存在事务将回滚事务");
        if (transactionUtils.isExistingTransaction()) {
            transactionUtils.rollback();
        }
    }
}
```



**事务工具类：**

```java
@Component
//@Scope("prototype")//不能为单例 可能会有线程安全问题,改为使用threodload解决线程安全问题
public class TransactionUtilsTest {

	private static ThreadLocal<TransactionStatus> transactionStatusThreadLocal
			= new ThreadLocal<>();

	@Autowired
	private DataSourceTransactionManager dataSourceTransactionManager;//拿到数据源接口

	// 开启事务
	public void begin() {
		TransactionStatus transaction
				= dataSourceTransactionManager
				.getTransaction(new DefaultTransactionAttribute());

		transactionStatusThreadLocal.set(transaction);
	}

	// 提交事务
	public void commit() {
		dataSourceTransactionManager.commit(transactionStatusThreadLocal.get());
	}

	// 回滚事务
	public void rollback() {
		dataSourceTransactionManager.rollback(transactionStatusThreadLocal.get());
	}

	public boolean isExistingTransaction() {
		try {
			if (transactionStatusThreadLocal.get() != null) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}
```



**xml配置文件以及业务层、持久层：**

```java
//xml配置文件和上个相同，业务层和持久层略
```

