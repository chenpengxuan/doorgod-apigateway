package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/26.
 */
public class TargetServer extends PrintFriendliness {
    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static TargetServer defaultInstance( ) {
        TargetServer server = new TargetServer();
        server.setPort(80);
        server.setHost("localhost");
        return server;
    }
}
