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
 * 自动扫描指定package下标注了{@link ChannelHandlerAutoBind}注解的{@link ChannelHandler}实现类
 * 并绑定到{@link ChannelPipeline}
 *
 * 绑定的顺序依赖于{@link ChannelHandlerAutoBind#indexAtChannel()}，值越小在ChannelPipeline中的位置越靠前
 *
 * 目前只支持绑定具有默认构造函数的ChannelHandler实例
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ChannelHandlerAutoBindProcessor {

    // 被扫描到的自定义ChannelHandler类的元信息
    private List<ChannelHandlerMeta> handlerClasses;
    // 指定扫描自定义ChannelHandler的包和子包
    private String                   scanPackage;


    public ChannelHandlerAutoBindProcessor(String scanPackage) {
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
        for (ChannelHandlerMeta meta : handlerClasses) {
            if (StringUtils.isEmpty(meta.getName())) {
                pipeline.addLast(meta.getHandler().newInstance());
            } else {
                pipeline.addLast(meta.getName(), meta.getHandler().newInstance());
            }
        }
    }

    /**
     * 处理指定包中标注了{@link ChannelHandlerAutoBind}注解的{@link ChannelHandler}实现类
     *
     * 加载后的实现类会按照{@link ChannelHandlerAutoBind#indexAtChannel()}排序，值越小位置越靠前
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
            if (clazz.isAnnotationPresent(ChannelHandlerAutoBind.class) && ChannelHandler.class.isAssignableFrom(clazz)) {
                ChannelHandlerAutoBind annotation = clazz.getAnnotation(ChannelHandlerAutoBind.class);
                String handlerName = annotation.handlerName();
                int handlerIndex = annotation.indexAtChannel();

                // 如果没有指定handlerName，默认为class名
                handlerName = StringUtils.isEmpty(handlerName) ? clazz.getName() : handlerName;
                // 包装为ChannelHandlerMeta实例，并汇总到列表中
                handlerClasses.add(new ChannelHandlerMeta((Class<? extends ChannelHandler>) clazz, handlerName, handlerIndex));
            }
        }
        // 按照index排序ChannelHandler
        Collections.sort(handlerClasses, Comparator.comparingInt(ChannelHandlerMeta::getIndex));
    }
}
