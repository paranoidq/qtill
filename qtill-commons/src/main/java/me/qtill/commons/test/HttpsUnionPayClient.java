package me.qtill.commons.test;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @ClassName HttpClient
 * @Description acpsdk发送后台http请求类
 * @date 2016-7-22 下午4:03:25
 */
public class HttpsUnionPayClient {
    private static Logger logger = Logger.getLogger(HttpsUnionPayClient.class);

    static {
//        System.setProperty("https.protocols", "SSLv3");
    }

    public static synchronized SSLContext getSslContext() {
        SSLContext sslContext = null;
        try {
            if (sslContext == null) {
                sslContext = SSLContext.getInstance("SSL");
                // 跳过证书验证
                TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
                };
                sslContext.init(null, trustManagers, new SecureRandom());
            }
        } catch (Exception e) {
            logger.error("初始化【 SSLContent】 失败！", e);
        }
        return sslContext;
    }

    /**
     * 创建连接
     *
     * @return
     * @throws IOException
     */
    @SuppressWarnings("finally")
    public static String sendMessToUnionpay(String reqUrl, String mess) throws IOException {
        //发送后台请求数据
        PrintStream out = null;
        InputStream in = null;
        URL url = null;
        HttpsURLConnection httpURLConnection = null;
        String responseMess = "";
        try {
            url = new URL(reqUrl);
            httpURLConnection = (HttpsURLConnection) url.openConnection();
            if (null == httpURLConnection) {
                logger.warn("httpURLConnection is null");
                return null;
            }
            httpURLConnection.setConnectTimeout(30000);// 连接超时时间
            httpURLConnection.setReadTimeout(30000);// 读取结果超时时间
            httpURLConnection.setDoInput(true); //可读
            httpURLConnection.setDoOutput(true); //可写
            httpURLConnection.setUseCaches(false);//取消缓存
            httpURLConnection.setRequestProperty("Content-Type", "x-ISO-TPDU/x-auth");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("HOST", "xjic.pos.95516.com:443");
            httpURLConnection.setRequestProperty("Accept", "*/*");
            httpURLConnection.setRequestProperty("User-Agent", "Donjin Http 0.1");
            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
            httpURLConnection.setRequestProperty("Content-Length", "62");
            httpURLConnection.setRequestProperty("Connection", "close");
            if ("https".equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
                //是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false
                if (true) {
//                    husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
                    husn.setSSLSocketFactory(getSslContext().getSocketFactory());
                    //解决由于服务器证书问题导致HTTPS无法访问的情况
                    husn.setHostnameVerifier(new BaseHttpSSLSocketFactory.TrustAnyHostnameVerifier());

                }
                httpURLConnection = husn;
            }

//            SSLSocket socket = (SSLSocket) getSslContext().getSocketFactory().createSocket();
//            SocketAddress address = new InetSocketAddress("xjic.pos.95516.com", 443);
//            socket.connect(address, 30000);

            //获取输出流
            try {
                httpURLConnection.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            OutputStream out1 = httpURLConnection.getOutputStream();
            byte[] bytes = HexString2Bytes(mess);
            out1.write(bytes, 0, bytes.length);
            out1.flush();
            out1.close();

            //获取响应输入流
            StringBuilder sb = new StringBuilder();
            if (200 == httpURLConnection.getResponseCode()) {
                in = httpURLConnection.getInputStream();
                responseMess = bytes2Hex(read(in));
            } else {
                in = httpURLConnection.getInputStream();
                //in = httpURLConnection.getErrorStream();
                sb.append(new String(read(in), "UTF-8"));
                logger.warn("银联接口响应失败Response message:[" + sb.toString() + "]");
                responseMess = null;
            }
            in.close();
            httpURLConnection.disconnect();
            System.out.println("responseMess:" + responseMess);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                out.close();
            }
            if (null != in) {
                in.close();
            }
            return responseMess;
        }
    }


    public static void main(String[] args) {
        String mess = "003C600620000060310031131208000020000000C00012000507353535353535353530313236303231323036333330363200110000000000300003303031";
        //锦州正式地址
        String reqUrl = "https://xjic.pos.95516.com:443/mjc/webtrans/VPB_lb";
        //测试地址
        // String reqUrl="https://101.231.114.192:16004/mjc/webtrans/VPB_lb";
        try {
            System.out.println("ResponseMess:" + sendMessToUnionpay(reqUrl, mess));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static byte[] read(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int length = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((length = in.read(buf, 0, buf.length)) > 0) {
            bout.write(buf, 0, length);
        }
        bout.flush();
        return bout.toByteArray();
    }

    public static byte[] HexString2Bytes(String hexstr) {
//
//        byte[] b = new byte[hexstr.length() / 2];
//        int j = 0;
//
//        for (int i = 0; i < b.length; i++) {
//            char c0 = hexstr.charAt(j++);
//            char c1 = hexstr.charAt(j++);
//
//            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
//        }
//
//        return b;
        try {
            return Hex.decodeHex(hexstr);
        } catch (DecoderException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 将btye数组转换成十六进制字符串
     *
     * @param bts
     * @return String
     */
    public static String bytes2Hex(byte[] bts) {
        StringBuilder strBuild = new StringBuilder();
        for (int i = 0; i < bts.length; i++) {
            strBuild.append(String.format("%02x", (bts[i] & 0xFF)));
        }
        return strBuild.toString();
    }

    private static int parse(char c) {
        if (c >= 'a') {
            return (c - 'a' + 10) & 0x0f;
        }

        if (c >= 'A') {
            return (c - 'A' + 10) & 0x0f;
        }

        return (c - '0') & 0x0f;
    }

}