package me.qtill.commons.codec;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Charsets {

    public static final Charset GBK        = Charset.forName("GBK");
    public static final Charset UTF_8      = Charset.forName("UTF-8");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");


    /**
     * 通过string获取Charset对象
     * @param charset
     * @return
     */
    public static Charset of(String charset) {
        Preconditions.checkArgument(StringUtils.isEmpty(charset), "charset is null");
        return Charset.forName(charset);
    }

}
