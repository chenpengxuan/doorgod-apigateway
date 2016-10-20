package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/10/20.
 */
public class HostUri {

    private String host;

    private String uri;

    public HostUri( String host, String uri ) {
        this.host = host == null ? "" : host.toLowerCase();
        this.uri = uri == null ? "" : uri.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostUri hostUri = (HostUri) o;

        if (!host.equals(hostUri.host)) return false;
        return uri.equals(hostUri.uri);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
