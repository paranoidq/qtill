package me.qtill.emps;

import org.apache.commons.codec.binary.Base64;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ResultBase64 {

    public static void main(String[] args) {
        String base64 = "WVdGaFlXRT0=";
        String ret = new String(Base64.decodeBase64(base64));
        System.out.println(ret);

    }
}
