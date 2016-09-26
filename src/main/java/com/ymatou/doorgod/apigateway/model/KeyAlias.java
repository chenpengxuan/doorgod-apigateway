package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/18.
 */
public class KeyAlias extends PrintFriendliness implements Comparable<KeyAlias> {
    private String uri;
    private String key;
    private String alias;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }


    /**
     * uri越长，越靠前，越优先匹配
     * @param o
     * @return
     */
    @Override
    public int compareTo(KeyAlias o) {
        return o.uri.length() - uri.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyAlias keyAlias = (KeyAlias) o;

        return uri != null ? uri.equals(keyAlias.uri) : keyAlias.uri == null;

    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }

    public boolean applicable( String uri ) {
        return this.uri.startsWith(uri);
    }
}
