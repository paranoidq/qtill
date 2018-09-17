package me.qtill.config;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import me.qtill.config.annotation.ConfigSource;
import me.qtill.config.annotation.Default;
import me.qtill.config.annotation.Key;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 配置管理工具类
 * <p>
 * 创建xxxConfig的代理实例，并注入对应的.properties配置文件项目
 * 通过调用{@link #get(Class)}获取代理实例
 */
public final class ConfigSupport {
    private static Logger logger = LoggerFactory.getLogger(ConfigSupport.class);

    // 缓存配置接口的代理实例
    private static volatile HashMap<Class<?>, Object>            configCache    = new HashMap<Class<?>, Object>();
    // 缓存配置接口的代理实例对应的Interceptor，用于在刷新时直接替换Interceptor，从而支持配置值的动态刷新功能
    private static volatile HashMap<Class<?>, ConfigInterceptor> interceptorMap = new HashMap<Class<?>, ConfigInterceptor>();

    /**
     * 私有构造器
     */
    private ConfigSupport() {
    }

    /**
     * 单例模式
     */
    private static class Holder {
        public static ConfigSupport configSupport = new ConfigSupport();
    }

    /**
     * 获取单例实例
     *
     * @return
     */
    public static ConfigSupport getInstance() {
        return Holder.configSupport;
    }

    /**
     * 获取配置类实例
     * <p>
     * 配置类实例作为单例缓存
     *
     * @param clazz
     * @return
     */
    public <T> T get(Class<T> clazz) {
        if (configCache.containsKey(clazz)) {
            return (T) configCache.get(clazz);
        }

        // 对配置类加锁
        // 加载不同的配置类的线程可以并发进行，但加载同一配置类的线程只能有一个最终执行创建和插入，保证并发的同时确保配置类实例的唯一
        synchronized (clazz) {
            if (configCache.containsKey(clazz)) {
                return (T) configCache.get(clazz);
            }

            // 加载配置文件
            String path = clazz.getAnnotation(ConfigSource.class).value();
            Properties properties = loadProperties(path);

            // 创建代理类，并放入缓存
            ConfigInterceptor interceptor = new ConfigInterceptor(properties);
            T config = createCglibDynamicProxy(clazz, interceptor);

            configCache.put(clazz, config);
            interceptorMap.put(clazz, interceptor);
        }
        return (T) configCache.get(clazz);
    }

    /**
     * 刷新配置文件
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> void refresh(final Class<T> clazz) {
        // 同步配置类，确保最多一个线程进行刷新操作
        synchronized (clazz) {
            // 加载配置文件
            String path = clazz.getAnnotation(ConfigSource.class).value();
            Properties properties = loadProperties(path);

            // 替换MethodInterceptor中的properties实例
            // xxxConfig通过动态代理的方式引用properties实例，刷新时，用新的properties直接替换
            // 不能通过创建新的xxxConfig代理实例来执行刷新操作，因为业务代码有可能将xxxConfig缓存为类变量或实例变量，而不是每次东从缓存中重新获取实例
            interceptorMap.get(clazz).refresh(properties);
            logger.info("Config [{}] refreshed", clazz.getName());
        }
    }

    /**
     * 刷新所有配置类
     */
    public void refreshAll() {
        Set<Class<?>> clazzSet = configCache.keySet();
        for (Class<?> clazz : clazzSet) {
            refresh(clazz);
        }
    }


    private volatile boolean                             autoRefresh      = false;
    private          ScheduledExecutorService            scheduler        = Executors.newSingleThreadScheduledExecutor();
    private static   ConcurrentHashMap<Class<?>, Object> autoRefreshCache = new ConcurrentHashMap<>();

    /**
     * 开启自动刷新
     *
     * @param configClazz
     * @param interval
     * @param unit
     * @param <T>
     */
    public <T> void enableAutoRefresh(Class<T> configClazz, long interval, TimeUnit unit) {
        if (autoRefresh) {
            logger.info("Auto refresh already enabled");
            return;
        }

        // 添加到自动刷新列表
        if (configClazz != null) {
            logger.info("Auto refresh enabled for [{}]", configClazz.getName());
            autoRefreshCache.put(configClazz, "");
        } else {
            logger.info("Auto refresh enabled for all configs");
            Set<Class<?>> clazzes = configCache.keySet();
            for (Class<?> clazz : clazzes) {
                autoRefreshCache.put(clazz, "");
            }
        }

        // 开启自动刷新，从autoRefreshCache中逐个进行刷新操作
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (autoRefreshCache.size() > 0) {
                    Set<Class<?>> autoRefreshConfigs = autoRefreshCache.keySet();
                    for (Class<?> clazz : autoRefreshConfigs) {
                        refresh(clazz);
                    }
                } else {
                    logger.warn("Auto refresh is disabled for any config. Wait unil next interval");
                }
            }
        }, interval, interval, unit);
        autoRefresh = true;
    }

    /**
     * 开启所有配置文件的自动刷新
     *
     * @param interval
     * @param unit
     */
    public void enableAutoRefresh(long interval, TimeUnit unit) {
        enableAutoRefresh(null, interval, unit);
    }


    /**
     * 关闭所有配置文件的自动刷新
     */
    public void disableAutoRefresh() {
        disableAutoRefresh(null);
    }

    /**
     * 关闭指定配置类的自动刷新
     *
     * @param configClazz
     */
    public void disableAutoRefresh(Class<?> configClazz) {
        if (configClazz == null) {
            logger.info("Auto refresh disabled for all configs");
            autoRefreshCache.clear();
        } else {
            autoRefreshCache.remove(configClazz);
            logger.info("Auto refresh disabled for [{}]", configClazz.getName());
        }
    }


    /**
     * Config类代理类
     */
    private static class ConfigInterceptor implements MethodInterceptor {
        private volatile Properties properties;

        public ConfigInterceptor(Properties properties) {
            this.properties = properties;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            // 如果没有标注Key注解，则使用函数名作为属性的key
            Key propKeyAnnotation = method.getAnnotation(Key.class);
            String propKey;
            if (propKeyAnnotation != null) {
                propKey = propKeyAnnotation.value();
            } else {
                propKey = method.getName();
            }

            // 先取配置文件的属性值，如果为空则采用DefaultValue注解标注的默认值
            String propValueString = null;
            if (properties != null) {
                propValueString = properties.getProperty(propKey);
            }
            if (propValueString == null) {
                Default defaultValue = method.getAnnotation(Default.class);
                if (defaultValue != null) {
                    propValueString = defaultValue.value();
                }
            }

            if (propValueString == null) {
                throw new IllegalStateException("Property [" + propKey + "] is neither given nor has a default value");
            }

            // 根据接口函数返回值，将属性值转换为指定类型
            Class<?> returnType = method.getReturnType();
            if (returnType == String.class) {
                return propValueString;
            }
            return convert(propValueString, returnType);
        }

        public void refresh(Properties properties) {
            this.properties = properties;
        }
    }

    /**
     * 加载配置文件
     *
     * @param path
     * @return
     */
    private Properties loadProperties(String path) {
        if (path.startsWith("classpath:")) {
            path = path.substring(10); // 跳过classpath部分
        }
        Properties properties = new Properties();
        URL url = getClasspathURL(path);
        if (url == null) {
            logger.error("Cannot find properties file: [{}]", path);
            return properties;
        }
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Cannot load properties: [{}]", e, path);
        } finally {
            Closeables.closeQuietly(inputStream);
        }
        return properties;
    }

    /**
     * 类型转换
     *
     * @param text
     * @param targetType
     * @param <T>
     * @return
     */
    private static <T> T convert(String text, Class<T> targetType) {
        Converter[] converters = Converter.values();
        for (Converter converter : converters) {
            try {
                Object value = converter.tryConvert(text, targetType);
                if (value != null && value != Converter.SKIP || value != Converter.NULL) {
                    return (T) value;
                }
            } catch (Exception e) {
                continue;
            }
        }
        throw new IllegalStateException("Cannot convert text to targetType: " + targetType.getName());
    }


    /**
     * 创建cglib代理
     *
     * @param clazzType
     * @param methodInterceptor
     * @param <T>
     * @return
     */
    private <T> T createCglibDynamicProxy(Class<T> clazzType, MethodInterceptor methodInterceptor) {
        Preconditions.checkNotNull(clazzType);
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(methodInterceptor);
        enhancer.setSuperclass(clazzType);
        return clazzType.cast(enhancer.create());
    }


    /**
     * 获取基于classpath的路径的URL对象
     *
     * @param path
     * @return
     */
    private static URL getClasspathURL(String path) {
        try {
            return Thread.currentThread().getContextClassLoader().getResource(path);
        } catch (Exception e) {
            logger.error("Get classpath-based url error. path=[{}]", e, path);
        }
        return null;
    }
}

