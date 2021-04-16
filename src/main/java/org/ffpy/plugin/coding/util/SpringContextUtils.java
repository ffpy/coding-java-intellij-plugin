package org.ffpy.plugin.coding.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Spring上下文工具类
 */
public class SpringContextUtils implements InitializingBean, ApplicationContextAware {

    /** Spring环境 */
    private volatile static Environment environment;

    /** Spring容器 */
    private volatile static ApplicationContext applicationContext;

    /** {@link ApplicationContext}观察者队列 */
    private static List<Consumer<ApplicationContext>> contextConsumers = new LinkedList<>();

    /** {@link Environment}观察者队列 */
    private static List<Consumer<Environment>> environmentConsumers = new LinkedList<>();

    private static ReadWriteLock contextLock = new ReentrantReadWriteLock();

    private static ReadWriteLock environmentLock = new ReentrantReadWriteLock();

    @Autowired
    private Environment mEnvironment;

    public static Environment getEnvironment() {
        if (environment == null) {
            throw new RuntimeException("Environment还没有初始化");
        }
        return environment;
    }

    public static void getEnvironmentAsync(Consumer<Environment> consumer) {
        if (environment == null) {
            Lock lock = environmentLock.writeLock();
            try {
                lock.lock();
                if (environment == null) {
                    environmentConsumers.add(consumer);
                } else {
                    consumer.accept(environment);
                }
            } finally {
                lock.unlock();
            }
        } else {
            consumer.accept(environment);
        }
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new RuntimeException("ApplicationContext还没有初始化");
        }
        return applicationContext;
    }

    /**
     * 用于在Spring执行setApplicationContext前使用ApplicationContext的场景
     */
    public static void getApplicationContextAsync(Consumer<ApplicationContext> consumer) {
        if (applicationContext == null) {
            Lock lock = contextLock.writeLock();
            try {
                lock.lock();
                if (applicationContext == null) {
                    contextConsumers.add(consumer);
                } else {
                    consumer.accept(applicationContext);
                }
            } finally {
                lock.unlock();
            }
        } else {
            consumer.accept(applicationContext);
        }
    }

    public static <T> T getBean(Class<T> type) {
        return (T) getApplicationContext().getBean(type);
    }

    /**
     * 用于在Spring执行setApplicationContext前使用ApplicationContext的场景
     */
    public static <T> void getBeanAsync(Class<T> type, Consumer<T> consumer) {
        getApplicationContextAsync(context -> consumer.accept(context.getBean(type)));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (environment != null) {
            return;
        }

        // 注入Environment
        environment = mEnvironment;

        Lock lock = environmentLock.readLock();
        try {
            lock.lock();
            environmentConsumers.forEach(consumer -> consumer.accept(environment));
            environmentConsumers = null;
            environmentLock = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        if (applicationContext != null) {
            return;
        }

        // 注入applicationContext
        applicationContext = context;

        Lock lock = contextLock.readLock();
        try {
            lock.lock();
            contextConsumers.forEach(consumer -> consumer.accept(context));
            contextConsumers = null;
            contextLock = null;
        } finally {
            lock.unlock();
        }
    }
}
