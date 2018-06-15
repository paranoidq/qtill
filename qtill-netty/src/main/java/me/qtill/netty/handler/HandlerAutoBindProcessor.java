package me.qtill.netty.handler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 *
 * 自动扫描指定package下标注了{@link HandlerAutoBind}注解的{@link ChannelHandler}实现类
 * 并绑定到{@link ChannelPipeline}
 *
 * 绑定的顺序依赖于{@link HandlerAutoBind#indexAtChannel()}，值越小在ChannelPipeline中的位置越靠前
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class HandlerAutoBindProcessor {

    private List<HandlerClassWrapper> handlerClasses;
    private String scanPackage;

    public HandlerAutoBindProcessor(String scanPackage) {
        this.scanPackage = scanPackage;
        init();
    }


    private void init() {
        this.handlerClasses = Lists.newArrayList();
    }


    /**
     * 绑定ChannelHandler到指定的{@link ChannelPipeline}上
     *
     * @param pipeline
     * @throws Exception
     */
    public void autoBind(ChannelPipeline pipeline) throws Exception {
        process(scanPackage);
        for (HandlerClassWrapper wrapper : handlerClasses) {
            pipeline.addLast(wrapper.getName(), wrapper.getHandler().newInstance());
        }
    }

    /**
     * 处理指定包中标注了{@link HandlerAutoBind}注解的{@link ChannelHandler}实现类
     *
     * 加载后的实现类会按照{@link HandlerAutoBind#indexAtChannel()}排序，值越小位置越靠前
     *
     * @param basePackage
     * @return
     * @throws IOException
     */
    private void process(String basePackage) throws IOException {
        ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
        ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(basePackage);

        for (ClassPath.ClassInfo classInfo : classes) {
            Class<?> clazz = classInfo.load();
            if (clazz.isAnnotationPresent(HandlerAutoBind.class) && ChannelHandler.class.isAssignableFrom(clazz)) {
                HandlerAutoBind annotation = clazz.getAnnotation(HandlerAutoBind.class);
                String handlerName = annotation.handlerName();
                int handlerIndex = annotation.indexAtChannel();

                // 如果没有指定handlerName，默认为class名
                handlerName = StringUtils.isEmpty(handlerName) ? clazz.getName() : handlerName;

                HandlerClassWrapper wrapper = new HandlerClassWrapper(
                    (Class<? extends ChannelHandler>) clazz, handlerName, handlerIndex
                );
                handlerClasses.add(wrapper);
            }
        }
        // sort
        Collections.sort(handlerClasses, Comparator.comparingInt(HandlerClassWrapper::getIndex));
    }
}
