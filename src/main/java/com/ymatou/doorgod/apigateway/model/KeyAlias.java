package com.ymatou.doorgod.apigateway.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Created by tuwenjie on 2016/9/18.
 */
public class KeyAlias extends PrintFriendliness implements Comparable<KeyAlias> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyAlias.class);
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
        try {
            return uri.startsWith(this.uri) || Pattern.matches(this.uri, uri);
        } catch (Exception e ) {
            LOGGER.error("Wrong uri pattern {} in KeyAlias config", this.uri, e);
            return false;
        }
    }
}
