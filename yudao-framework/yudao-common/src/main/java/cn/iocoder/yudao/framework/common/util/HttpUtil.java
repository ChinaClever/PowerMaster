package cn.iocoder.yudao.framework.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author luowei
 * @version 1.0
 * @description: Http请求
 * @date 2024/4/30 9:30
 */
@Slf4j
@Component
public class HttpUtil {


    /**
     * get请求
     *
     * @param addr
     */
    public static void get(String addr) {
        BufferedReader in = null;
        try {
            //创建信任管理器，忽略证书认证
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // 获取默认的 SSL 上下文实例
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // 初始化 SSL 上下文，并指定信任管理器
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // 获取 SSL socket 工厂
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // 设置默认的 SSL socket 工厂
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            // 创建主机名验证器，用于禁用主机名验证
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            // 设置默认的主机名验证器
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            // 创建 URL 对象并进行连接
            URL url = new URL("http://" + addr);
            log.info("url : " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    log.info("url:" +url.getPath() );
            conn.setRequestMethod("GET");
            //Get请求不需要DoOutPut
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.connect();
            log.info("code : " + conn.getResponseCode());

        } catch (Exception e) {
            log.error("异常：", e);
        }

    }

}
