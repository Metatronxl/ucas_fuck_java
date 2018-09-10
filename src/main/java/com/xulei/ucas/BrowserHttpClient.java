package com.xulei.ucas;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.commons.lang.StringUtils;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @author lei.X
 * @date 2018/9/10
 */
@Slf4j
public class BrowserHttpClient {


    private final static int SOCKETTIMEOUT = 10000;
    private final static int CONNECTIONTIMEOUT = 5000;
    private final static int CONNREQUESTTIMEOUT = 5000;
    private final static int SOCKETSOTIMEOUT = 15000;

    private final static int PROXY_RECONNECT_CODE = 601;

    private CloseableHttpClient browserHttpClient = null;
    private RequestConfig requestConfig = null;
    private BasicCookieStore cookieStore = null;


    public BrowserHttpClient(String ip, int port, Cookie[] cookies){
        cookieStore = new BasicCookieStore();
        RequestConfig.Builder builder = RequestConfig.custom().setSocketTimeout(SOCKETSOTIMEOUT)
                .setConnectionRequestTimeout(CONNREQUESTTIMEOUT)
                .setConnectTimeout(CONNECTIONTIMEOUT)
                .setCookieSpec(CookieSpecs.DEFAULT);
        if (cookies != null && cookies.length >0){
            cookieStore.addCookies(cookies);
        }
        if (!StringUtils.isEmpty(ip)){
            HttpHost proxy = new HttpHost(ip,port);
            requestConfig = builder.setProxy(proxy).build();
        }else {
            requestConfig= builder.build();
        }
        this.browserHttpClient = createHttpsClient();

    }

    public BrowserHttpClient(String ip, int port, Cookie[] cookies, int conTimeout, int socketTimeout) {
        cookieStore = new BasicCookieStore();
        RequestConfig.Builder builder = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(CONNREQUESTTIMEOUT)
                .setConnectTimeout(conTimeout).setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);
        if (cookies != null && cookies.length > 0) {
            cookieStore.addCookies(cookies);
        }
        if (StringUtils.isEmpty(ip)) {
            HttpHost proxy = new HttpHost(ip, port);
            requestConfig = builder.setProxy(proxy).build();
        } else {
            requestConfig = builder.build();
        }
        this.browserHttpClient = createHttpsClient();
    }

    public BrowserHttpClient(String ip, int port, Cookie[] cookies, int conTimeout, int socketTimeout, Boolean isRedirect) {
        cookieStore = new BasicCookieStore();
        RequestConfig.Builder builder = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(CONNREQUESTTIMEOUT)
                .setConnectTimeout(conTimeout).setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                .setRedirectsEnabled(isRedirect).setCircularRedirectsAllowed(isRedirect);
        if (cookies != null && cookies.length > 0) {
            cookieStore.addCookies(cookies);
        }
        if (StringUtils.isNotBlank(ip)) {
            HttpHost proxy = new HttpHost(ip, port);
            requestConfig = builder.setProxy(proxy).build();
        } else {
            requestConfig = builder.build();
        }
        if (isRedirect) {
            this.browserHttpClient = createRedirectHttpsClient();
        } else {
            this.browserHttpClient = createHttpsClient();
        }
    }

    public BrowserHttpClient(String ip, int port, Cookie[] cookies, int conTimeout, int socketTimeout, Charset charset) {
        cookieStore = new BasicCookieStore();
        RequestConfig.Builder builder = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(CONNREQUESTTIMEOUT)
                .setConnectTimeout(conTimeout).setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);
        if (cookies != null && cookies.length > 0) {
            cookieStore.addCookies(cookies);
        }
        if (StringUtils.isEmpty(ip)) {
            HttpHost proxy = new HttpHost(ip, port);
            requestConfig = builder.setProxy(proxy).build();
        } else {
            requestConfig = builder.build();
        }
        this.browserHttpClient = createHttpsClient(charset);
    }

    public void updateCookieStore(Cookie[] cookies){
        cookieStore.addCookies(cookies);
    }

    public void updateCookieStore(String str){
        if (!StringUtils.isEmpty(str)){
            String[] strs = str.split(";");
            for (int i=0;i<strs.length;i++){
                int option = strs[i].indexOf("=");
                int length = strs[i].length();
                BasicClientCookie cookie = new BasicClientCookie(strs[i].substring(0,option),strs[i].substring(option+1,length));
//                cookie.setDomain("ch.com");
                cookieStore.addCookie(cookie);
            }
        }
    }

    public CookieStore getCookieStore(){
        return cookieStore;
    }

    public String getNoProxy(String url) throws IOException {
        RequestConfig.Builder builder = RequestConfig.custom().setSocketTimeout(SOCKETTIMEOUT)
                .setConnectionRequestTimeout(CONNREQUESTTIMEOUT)
                .setConnectTimeout(CONNECTIONTIMEOUT).setCookieSpec(CookieSpecs.DEFAULT);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(builder.build());
        Integer statusCode;
        try {
            HttpResponse httpResponse = browserHttpClient.execute(httpGet);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            log.info("statusCode:{}", statusCode);
            return EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8.toString());
        } finally {
            httpGet.releaseConnection();
        }
    }

    public String get(String url, Header[] headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        Integer statusCode = HttpStatus.SC_OK;
        if (headers != null && headers.length > 0) {
            httpGet.setHeaders(headers);
        }

        try {
            HttpResponse httpResponse = browserHttpClient.execute(httpGet);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            log.info("statusCode:{}", statusCode);
            if (statusCode == PROXY_RECONNECT_CODE) {
                return "";
            }
            return EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8.toString());
        } catch (IOException e) {
            statusCode = 500;
            throw e;
        } finally {
            httpGet.releaseConnection();
        }
    }

    public int getStatus(String url, Header[] headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        if (headers != null && headers.length > 0) {
            httpGet.setHeaders(headers);
        }

        try {
            HttpResponse httpResponse = browserHttpClient.execute(httpGet);
            int code = httpResponse.getStatusLine().getStatusCode();
            log.info("statusCode:{}", code);
            return code;
        } finally {
            httpGet.releaseConnection();
        }
    }

    public String get(String url, Header[] headers, int retryCount) throws IOException {
        for (int i = 0; i < retryCount; i++) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            if (headers != null && headers.length > 0) {
                httpGet.setHeaders(headers);
            }
            try {
                HttpResponse httpResponse = browserHttpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                log.info("statusCode:{}", code);
                String response = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8.toString());
                if (code == PROXY_RECONNECT_CODE) {
                    response = null;
                }
                if (response != null && response.contains("The requested URL could not be retrieved")
                        && i < retryCount - 1) {
                    log.warn("可能是代理IP异常，重试:{}", response);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("sleep异常", e);
                    }
                    continue;
                }
                return response;
            } catch (IOException e) {
                if (i == retryCount - 1) {
                    throw e;
                } else {
                    log.warn("请求网络异常重试", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        log.error("sleep异常", e2);
                    }
                    continue;
                }
            } finally {
                httpGet.releaseConnection();
            }
        }
        return null;
    }

    public byte[] getEntity(String url, Header[] headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        if (headers != null && headers.length > 0) {
            httpGet.setHeaders(headers);
        }
        try {
            HttpResponse httpResponse = browserHttpClient.execute(httpGet);
            int code = httpResponse.getStatusLine().getStatusCode();
            log.info("statusCode:{}", code);
            if (code == PROXY_RECONNECT_CODE) {
                return null;
            }
            return EntityUtils.toByteArray(httpResponse.getEntity());
        } finally {
            httpGet.releaseConnection();
        }
    }

    public String get(String url, Header[] headers, List<Header> resHeaders) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        if (headers != null && headers.length > 0) {
            httpGet.setHeaders(headers);
        }

        try {
            HttpResponse httpResponse = browserHttpClient.execute(httpGet);
            int code = httpResponse.getStatusLine().getStatusCode();
            log.info("statusCode:{}", code);
            resHeaders.addAll(Arrays.asList(httpResponse.getAllHeaders()));
            if (code == PROXY_RECONNECT_CODE) {
                return "";
            }
            return EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8.toString());
        } finally {
            httpGet.releaseConnection();
        }
    }

    public String post(String url, List<NameValuePair> nvPairs, Header[] headers) throws IOException {
        HttpPost httpPost = null;
        Header[] headerRes;
        try {
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new UrlEncodedFormEntity(nvPairs, Charsets.UTF_8.toString()));
            httpPost.setHeaders(headers);
            HttpResponse httpResp = browserHttpClient.execute(httpPost);
            int code = httpResp.getStatusLine().getStatusCode();
            log.info("statusCode:{}", code);
            if (code == PROXY_RECONNECT_CODE) {
                return "";
            }
            headerRes = httpResp.getAllHeaders();
            return EntityUtils.toString(httpResp.getEntity(), Charsets.UTF_8.toString());
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    public String post(String url, Map<String, String> parameter) throws IOException {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(mapToHttpEntity(parameter));

            HttpResponse httpResp = browserHttpClient.execute(httpPost);
            int code = httpResp.getStatusLine().getStatusCode();
            log.info("statusCode:{}", code);
            if (code == PROXY_RECONNECT_CODE) {
                return "";
            }
            return EntityUtils.toString(httpResp.getEntity(), Charsets.UTF_8.toString());

        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    public String post(String url, Map<String, String> parameter, Header[] headers, Charset charset) throws IOException {
        HttpPost httpPost = null;
        Header[] headerRes;
        int statusCode = 200;
        try {
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(mapToHttpEntity(parameter, charset));
            httpPost.setHeaders(headers);
            HttpResponse httpResp = browserHttpClient.execute(httpPost);
            log.info("statusCode:{}", httpResp.getStatusLine().getStatusCode());
            statusCode = httpResp.getStatusLine().getStatusCode();
            if (HttpStatus.SC_MOVED_TEMPORARILY == statusCode) {
                headerRes = httpResp.getAllHeaders();
            }
            if (statusCode == PROXY_RECONNECT_CODE) {
                return "";
            }
            return EntityUtils.toString(httpResp.getEntity(), charset);
        } catch (IOException e) {
            statusCode = 500;
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    public String postNoProxy(String url, Map<String, String> parameter, Header[] headers) throws IOException {
        HttpPost httpPost = null;
        Header[] headerRes;
        int statusCode = 200;
        try {
            httpPost = new HttpPost(url);
            RequestConfig.Builder builder = RequestConfig.custom().setSocketTimeout(SOCKETTIMEOUT)
                    .setConnectionRequestTimeout(CONNREQUESTTIMEOUT)
                    .setConnectTimeout(CONNECTIONTIMEOUT).setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);
            httpPost.setConfig(builder.build());
            httpPost.setEntity(mapToHttpEntity(parameter));
            httpPost.setHeaders(headers);
            HttpResponse httpResp = browserHttpClient.execute(httpPost);
            log.info("statusCode:{}", httpResp.getStatusLine().getStatusCode());
            statusCode = httpResp.getStatusLine().getStatusCode();
            if (HttpStatus.SC_MOVED_TEMPORARILY == statusCode) {
                headerRes = httpResp.getAllHeaders();
                if (null != headerRes && headerRes.length > 0) {
                    for (Header header : headerRes) {
                        if (StringUtils.equalsIgnoreCase(header.getName(), "Location")) {
                        }
                    }
                }
            }
            return EntityUtils.toString(httpResp.getEntity(), Charsets.UTF_8.toString());
        } catch (IOException e) {
            statusCode = 500;
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    public String post(String url, String content, Header[] headers) throws IOException {
        HttpPost httpPost = null;
        int statusCode = 200;
        try {
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new StringEntity(content, "UTF-8"));
            if (headers != null) {
                httpPost.setHeaders(headers);
            }
            HttpResponse httpResp = browserHttpClient.execute(httpPost);
            statusCode = httpResp.getStatusLine().getStatusCode();
            log.info("statusCode:{}", statusCode);
            if (statusCode == PROXY_RECONNECT_CODE) {
                return "";
            }
            if (HttpStatus.SC_MOVED_TEMPORARILY == statusCode) {
                Header[] resHeads = httpResp.getAllHeaders();
                if (null != resHeads && resHeads.length > 0) {
                    for (Header header : resHeads) {
                        if (StringUtils.equalsIgnoreCase(header.getName(), "Location")) {
                            log.info("302 relocation {}", header.getValue());
                        }
                    }
                }
            }
            return EntityUtils.toString(httpResp.getEntity(), Charsets.UTF_8.toString());
        } catch (IOException e) {
            statusCode = 500;
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    public HttpResponse httpPost(String url, Map<String, String> parameter, Header[] headers) throws IOException {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(mapToHttpEntity(parameter));
            httpPost.setHeaders(headers);
            CloseableHttpResponse response = browserHttpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            if (code == PROXY_RECONNECT_CODE) {
                return null;
            }
            return response;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    public String post(String url, Map<String, String> parameter, Header[] headers, int retryCount) throws IOException {
        for (int i = 0; i < retryCount; i++) {
            HttpPost httpPost = null;
            try {
                httpPost = new HttpPost(url);
                httpPost.setConfig(requestConfig);
                httpPost.setEntity(mapToHttpEntity(parameter));
                httpPost.setHeaders(headers);
                HttpResponse httpResp = browserHttpClient.execute(httpPost);
                int code = httpResp.getStatusLine().getStatusCode();
                log.info("statusCode:{}", code);
                String response = EntityUtils.toString(httpResp.getEntity(), Charsets.UTF_8.toString());
                if (code == PROXY_RECONNECT_CODE) {
                    response = null;
                }
                if (response != null && response.contains("The requested URL could not be retrieved")
                        && i < retryCount - 1) {
                    log.warn("可能是代理IP异常，重试:{}", response);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("sleep异常", e);
                    }
                    continue;
                }
                return response;
            } catch (IOException e) {
                if (i == retryCount - 1) {
                    throw e;
                } else {
                    log.warn("请求网络异常重试", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        log.error("sleep异常", e2);
                    }
                    continue;
                }
            } finally {
                if (httpPost != null) {
                    httpPost.releaseConnection();
                }
            }
        }
        return null;
    }

    public String post(String url, Map<String, String> parameter, Header[] headers, List<Header> resHeaders)
            throws IOException {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(mapToHttpEntity(parameter));
            httpPost.setHeaders(headers);
            HttpResponse httpResp = browserHttpClient.execute(httpPost);
            int code = httpResp.getStatusLine().getStatusCode();
            log.info("statusCode:{}", code);
            resHeaders.addAll(Arrays.asList(httpResp.getAllHeaders()));
            if (code == PROXY_RECONNECT_CODE) {
                return "";
            }
            return EntityUtils.toString(httpResp.getEntity());
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }


    public String post(String url, Map<String, String> parameter, Header[] headers) throws IOException {
        return post(url, parameter, headers,Charsets.UTF_8);
    }


    private HttpEntity mapToHttpEntity(Map<String, String> params) throws UnsupportedEncodingException {
        return mapToHttpEntity(params, Charsets.UTF_8);
    }

    private HttpEntity mapToHttpEntity(Map<String, String> params, Charset charSet) throws UnsupportedEncodingException {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> nvPairs = Lists.newArrayList();
            for (String key : params.keySet()) {
                nvPairs.add(
                        new BasicNameValuePair(StringUtils.trimToEmpty(key), StringUtils.trimToEmpty(params.get(key))));
            }
            return new UrlEncodedFormEntity(nvPairs, charSet.toString());
        }
        return null;
    }


    private CloseableHttpClient createRedirectHttpsClient() {
        X509TrustManager x509mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509mgr}, null);
        } catch (NoSuchAlgorithmException e) {
            log.error("https createHttpsClient NoSuchAlgorithmException:{}", e);
        } catch (KeyManagementException e1) {
            log.error("https createHttpsClient KeyManagementException:{}", e1);
        }

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoLinger(-1).
                setSoReuseAddress(false).setSoTimeout(SOCKETSOTIMEOUT).setTcpNoDelay(true).build();


        //防止http头部有中文
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Charsets.UTF_8).build();

        //防止url重定向的时候,url中存在ASCII码以外的字母
        NormalRedirectStrategy normalRedirectStrategy = new NormalRedirectStrategy();

        return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setDefaultCookieStore(cookieStore)
                .setMaxConnTotal(400)
                .setMaxConnPerRoute(50).setDefaultSocketConfig(socketConfig)
                .setDefaultConnectionConfig(connectionConfig)
                .setRedirectStrategy(normalRedirectStrategy).build();
    }

    private static class NormalRedirectStrategy extends LaxRedirectStrategy {

        /**
         * @since 4.1
         */
        @Override
        protected URI createLocationURI(String location) throws ProtocolException {
            //可能存在空格,这会导致java内置的URI解析逻辑报错,但是浏览器会自动处理这个行为
            return super.createLocationURI(location.replaceAll(" ", "%20"));
        }
    }




    private CloseableHttpClient createHttpsClient() {
        X509TrustManager x509mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509mgr}, null);

        } catch (NoSuchAlgorithmException e) {
            log.error("https createHttpsClient NoSuchAlgorithmException:{}", e);
        } catch (KeyManagementException e1) {
            log.error("https createHttpsClient KeyManagementException:{}", e1);
        }

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoLinger(-1).
                setSoReuseAddress(false).setSoTimeout(SOCKETSOTIMEOUT).setTcpNoDelay(true).build();


        //防止http头部有中文
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Charsets.UTF_8).build();


        return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setDefaultCookieStore(cookieStore)
                .setMaxConnTotal(400)
                .setMaxConnPerRoute(50).setDefaultSocketConfig(socketConfig).setDefaultConnectionConfig(connectionConfig)
                .build();
    }

    private CloseableHttpClient createHttpsClient(Charset charset) {
        X509TrustManager x509mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509mgr}, null);

//            Protocol protocol = new Protocol("https", new SSLProtocol(), 443);
//            Protocol.registerProtocol("https", protocol);
        } catch (NoSuchAlgorithmException e) {
            log.error("https createHttpsClient NoSuchAlgorithmException:{}", e);
        } catch (KeyManagementException e1) {
            log.error("https createHttpsClient KeyManagementException:{}", e1);
        }

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoLinger(-1).
                setSoReuseAddress(false).setSoTimeout(SOCKETSOTIMEOUT).setTcpNoDelay(true).build();

        return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setDefaultCookieStore(cookieStore)
                .setMaxConnTotal(400)
                .setMaxConnPerRoute(50).setDefaultSocketConfig(socketConfig)
                .setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(charset).build()).build();
    }


}
