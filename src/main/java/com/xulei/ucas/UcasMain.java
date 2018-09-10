package com.xulei.ucas;

import sun.rmi.runtime.Log;

/**
 * @author lei.X
 * @date 2018/9/10
 */
public class UcasMain {

    //连接超时时间
    private static final int CONNECT_TIMEOUT = 10000;
    //读超时时间
    private static final int SOCKET_TIMEOUT = 35000;

    public static final ThreadLocal<BrowserHttpClient> HTTP_CLIENT_THREAD_LOCAL = new ThreadLocal<BrowserHttpClient>();

    //UA
    public static final ThreadLocal<String> USER_AGENT = new ThreadLocal<String>();

    public static BrowserHttpClient getHttpClient() {
        return HTTP_CLIENT_THREAD_LOCAL.get();
    }

    public static void setHttpClient(BrowserHttpClient httpClient) {
        HTTP_CLIENT_THREAD_LOCAL.set(httpClient);
    }


    public static void login(String username,String password){

    }


    public static void main(String[] args) {
        BrowserHttpClient httpClient = new BrowserHttpClient("127.0.0.1", 8888, null, 10000, 10000, true);
        UcasMain.setHttpClient(httpClient);

    }



}
