package com.xulei.ucas;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.X
 * @date 2018/9/10
 */
@Slf4j
public class UcasMain {

    //连接超时时间
    private static final int CONNECT_TIMEOUT = 10000;
    //读超时时间
    private static final int SOCKET_TIMEOUT = 35000;

    private static  final String TAG = "CRAWL_XL";

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

        String login_url = Constants.LOGIN_URL;
        Map<String,String> loginMap = new HashMap<>();
        loginMap.put("userName",username);
        loginMap.put("pwd",password);
        loginMap.put("sb","sb");

        try {
            String result = HTTP_CLIENT_THREAD_LOCAL.get().post(Constants.LOGIN_URL,loginMap,Context.getHttpHeaderByHeader());
            System.out.println(result);
        } catch (IOException e) {
            log.error(TAG,e);
        }

    }


    public static void main(String[] args) {
        BrowserHttpClient httpClient = new BrowserHttpClient("127.0.0.1", 0, null, 10000, 10000, true);
        UcasMain.setHttpClient(httpClient);

        UcasMain.login("381827702@qq.com","woshixulei123");


    }



}
