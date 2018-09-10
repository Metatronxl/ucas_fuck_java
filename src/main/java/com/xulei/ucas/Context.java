package com.xulei.ucas;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lei.X
 * @date 2018/9/10
 */
public class Context {

    public static List<Header> getHttpHeader(){
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Host","sep.ucas.ac.cn"));
        headers.add(new BasicHeader("Pragma","no-cache"));
        headers.add(new BasicHeader("Connection", "keep-alive"));
        headers.add(new BasicHeader("Cache-Control", "max-age=0"));
        headers.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
        headers.add(new BasicHeader("Upgrade-Insecure-Requests","1"));
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));


        return headers;
    }

    public static Header[] getHttpHeaderByHeader(){
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Host","sep.ucas.ac.cn"));
        headers.add(new BasicHeader("Pragma","no-cache"));
        headers.add(new BasicHeader("Connection", "keep-alive"));
        headers.add(new BasicHeader("Cache-Control", "max-age=0"));
        headers.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
        headers.add(new BasicHeader("Upgrade-Insecure-Requests","1"));
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));

        return headers.toArray(new Header[headers.size()]);
    }




}
