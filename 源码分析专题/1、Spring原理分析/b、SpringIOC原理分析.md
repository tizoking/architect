# 前言：

涉及到的知识：

- dom4j解析xml
- ioc原理

> 感觉不是特别好，大概意思谁都知道。还是要看源码。。。

# 一、手写SpringIOC配置文件版



#### 1.1 ClassPahXmlApplication

```java
public class ClassPathXmlApplicationContext {

    private String xmlPath;//xml文件地址

    public ClassPathXmlApplicationContext(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public Object getBean(String beanId) throws Exception {
        if (StringUtils.isEmpty(beanId)) {
            throw new Exception("beanId不能为null！");
        }
        //1、解析xml文件,获取所以bean节点信息
        List<Element> readerXml = readerXml();
        if (readerXml == null || readerXml.isEmpty()) {
            throw new Exception("配置文件中，没有配置bean信息！");
        }

        //2、使用方法参数 bean id 查找配置文件中bean节点的id信息是否一致
        String elementClass = findByElementClass(readerXml, beanId);
        if (StringUtils.isEmpty(elementClass)) {
            throw new RuntimeException("配置文件中没有配置class地址");
        }

        return initBean(elementClass);
    }

    /**
     * 根据beanId查找对应的全限定类名
     * @param list
     * @param beanId
     * @return
     */
    private String findByElementClass(List<Element> list, String beanId) {
        for (Element element : list) {
            String xmlBeanId = element.attributeValue("id");
            //如果一致
            if (StringUtils.equals(xmlBeanId, beanId)) {
                String xmlClass = element.attributeValue("class");
                return xmlClass;
            }
        }
        return null;
    }

    /**
     * 反射创建类
     * @param className
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object initBean(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> forName = Class.forName(className);
        return forName.newInstance();
    }


    /**
     * 解析xml文件
     */
    private List<Element> readerXml() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        //读取xml文档
        Document document = saxReader.read(getResourceAsStream(xmlPath));

        //1.读取根结点
        Element rootElement = document.getRootElement();
        //2.读取根结点下所有的子节点
        List<Element> elements = rootElement.elements();
        return elements;
    }

    /**
     * 获取路径
     * @return
     */
    private InputStream getResourceAsStream(String xmlPath) {
        return this.getClass().getClassLoader()
                .getResourceAsStream(xmlPath);
    }
}
```



#### 1.2 service

```java
public class UserService {
    public void service() {
        System.out.println("哈哈哈！ 我被调用了");
    }
}
```

#### 1.3 xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="userService" class="club.maddm.service.UserService"/>
</beans>
```



#### 1.4 测试类

```java
/**
 * 测试手写SpringIOC
 */
public class TestClassPathXml {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext applicationContext
                = new ClassPathXmlApplicationContext("springapplication.xml");

        UserService userService
                = (UserService)applicationContext.getBean("userService");

        userService.service();

    }
}
```



# 二、手写SpringIOC注解版本



#### 2.1 工具类

```java
public class ClassUtil {

	/**
	 * 取得某个接口下所有实现这个接口的类
	 */
	public static List<Class> getAllClassByInterface(Class c) {
		List<Class> returnClassList = null;

		if (c.isInterface()) {
			// 获取当前的包名
			String packageName = c.getPackage().getName();
			// 获取当前包下以及子包下所以的类
			List<Class<?>> allClass = getClasses(packageName);
			if (allClass != null) {
				returnClassList = new ArrayList<Class>();
				for (Class classes : allClass) {
					// 判断是否是同一个接口
					if (c.isAssignableFrom(classes)) {
						// 本身不加入进去
						if (!c.equals(classes)) {
							returnClassList.add(classes);
						}
					}
				}
			}
		}

		return returnClassList;
	}

	/*
	 * 取得某一类所在包的所有类名 不含迭代
	 */
	public static String[] getPackageAllClassName(String classLocation, String packageName) {
		// 将packageName分解
		String[] packagePathSplit = packageName.split("[.]");
		String realClassLocation = classLocation;
		int packageLength = packagePathSplit.length;
		for (int i = 0; i < packageLength; i++) {
			realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
		}
		File packeageDir = new File(realClassLocation);
		if (packeageDir.isDirectory()) {
			String[] allClassName = packeageDir.list();
			return allClassName;
		}
		return null;
	}

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param packageName
	 * @return
	 */
	public static List<Class<?>> getClasses(String packageName) {

		// 第一个class类的集合
		List<Class<?>> classes = new ArrayList<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					JarFile jar;
					try {
						// 获取jar
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						// 从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						// 同样的进行循环迭代
						while (entries.hasMoreElements()) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							// 如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果以"/"结尾 是一个包
								if (idx != -1) {
									// 获取包名 把"/"替换成"."
									packageName = name.substring(0, idx).replace('/', '.');
								}
								// 如果可以迭代下去 并且是一个包
								if ((idx != -1) || recursive) {
									// 如果是一个.class文件 而且不是目录
									if (name.endsWith(".class") && !entry.isDirectory()) {
										// 去掉后面的".class" 获取真正的类名
										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try {
											// 添加到classes
											classes.add(Class.forName(packageName + '.' + className));
										} catch (ClassNotFoundException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
			List<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
						classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					// 添加到集合中去
					classes.add(Class.forName(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
```

#### 2.2 注解

```java
//自定义自动注入
@Target(ElementType.FIELD)//作用于类
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtResource {
}
//==========================================================//
//自定义service注解
@Target(ElementType.TYPE)//作用于类
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtService {
}
```

#### 2.3 Application

```java
// 手写Spring IOC注解版本
public class ExtClassPatnXmlApplicationContext {

    private String packageName;//扫描的包

    private static ConcurrentHashMap<String, Object> beans = null; //存放class地址

    //传入需要扫秒的包名
    public ExtClassPatnXmlApplicationContext(String packageName) {
        beans = new ConcurrentHashMap<>();
        if (StringUtils.isNotEmpty(packageName)) {
            this.packageName = packageName;
            initBeans();
        }
        //依赖注入
        for (String s : beans.keySet()) {
            attriAssign(beans.get(s));
        }
    }


    public Object getBean(String beanId) {
        if (StringUtils.isEmpty(beanId)) {
            throw new RuntimeException("beanId不能为空");
        }

        if (beans.get(beanId) == null) {
            throw new RuntimeException("该bean不存在");
        }
        return beans.get(beanId);
    }

    /**
     * 反射创建类
     * @param aClass
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object newInstance(Class<?> aClass) throws IllegalAccessException, InstantiationException {
        return aClass.newInstance();
    }




    //初始化对象
    private void initBeans() {
        //1、使用java的反射机制扫包，获取当前包下的所有类
        List<Class<?>> classes = ClassUtil.getClasses(packageName);

        //2、判断类上是否存在注入bean的注解
        ConcurrentHashMap<String, Object> classExiAnnotation
                = findClassExiAnnotation(classes);

        if (classExiAnnotation == null || classExiAnnotation.isEmpty()) {
            throw new RuntimeException("该包下没有任何类存在注解");
        }
    }


    /**
     * 判断类上是否存在注入bean的注解
     * @param classes
     * @return
     */
    private ConcurrentHashMap<String, Object> findClassExiAnnotation(List<Class<?>> classes) {
        for (Class<?> classInfo : classes) {
            //判断是否有注解
            ExtService annotation = classInfo.getAnnotation(ExtService.class);
            if (annotation != null) {
                //获取当前类名
                String className = classInfo.getSimpleName();
                String beanID = toLowerCaseFirstOne(className);
                //key:bean的id【类名小写】，value：实体类
                Object obj = null;
                try {
                    obj = newInstance(classInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (obj == null) {
                    throw new RuntimeException("将对象放入容器时失败。。。");
                }
                beans.put(beanID, obj);
            }
        }
        return beans;
    }

    /**
     * 依赖注入原理
     */
    private void attriAssign(Object object) {
        //1、使用反射机制，获取当前类的所有属性
        Field[] fields = object.getClass().getDeclaredFields();
        //2、判断当前类属性是否存在注解
        for (Field field : fields) {
            ExtResource fieldAnnotation = field.getAnnotation(ExtResource.class);
            if (fieldAnnotation != null) {
                //存在注解,获取属性名称
                String name = field.getName();
                //3、默认使用属性名称，查找bean容器对象
                Object bean = beans.get(name);//从容器中获取对象
                if (bean != null) {
                    try {
                        field.setAccessible(true);//允许访问似有属性
                        field.set(object,bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * 首字母转小写
     * @param s
     * @return
     */
    private static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }
}
```

#### 2.4 测试bean

```java
@ExtService
public class UserService {

    @ExtResource
    private UserMapper userMapper;

    public void service() {
        userMapper.save();
    }
}
//=======================================================================//
@ExtService
public class UserMapper {
    public void save() {
        System.out.println("保存方法被调用了");
    }
}
```

#### 2.5 测试main

```java
public class TestAnn {
    public static void main(String[] args) {
        ExtClassPatnXmlApplicationContext applicationContext
                = new ExtClassPatnXmlApplicationContext("club.maddm");

        UserService userService
                = (UserService) applicationContext.getBean("userService");

        userService.service();
    }
}
```







