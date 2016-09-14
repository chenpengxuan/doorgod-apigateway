/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 * 
 * All rights reserved.
 */
package com.ymatou.doorgod.apigateway.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;


/**
 * 自身内容能以可读方式输出
 * 
 * @author tuwenjie
 *
 */
public abstract class PrintFriendliness implements Serializable {

    private static final long serialVersionUID = -235822080790001901L;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":"
                + JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.SkipTransientField);
    }

}
