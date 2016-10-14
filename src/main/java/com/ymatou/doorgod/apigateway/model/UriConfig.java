package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class UriConfig extends PrintFriendliness implements Comparable<UriConfig> {

    //超时时间，以毫秒计
    private int timeout = -1;

    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * uri越长，越靠前匹配
     * @param o
     * @return
     */
    @Override
    public int compareTo(UriConfig o) {
        return o.uri.length() - this.uri.length();
    }
}
