package com.ymatou.doorgod.apigateway.model;

import java.util.Set;
import java.util.TreeMap;

/**
 * 从收到的http请求{@link io.vertx.core.http.HttpServerRequest}中提取的样本
 * Created by tuwenjie on 2016/9/7.
 */
public class Sample extends PrintFriendliness {

    public static final String NULL_VALUE_PLACEHOLDER = "NULL_FLAG";


    //样本值,按key字典序排序
    //<em>明确定义为TreeMap,使得fastjson反序列化时，使用TreeMap,否则会使用默认的HashMap</em>
    private TreeMap<String, String> dimensionValues = new TreeMap<String, String>( );


    public void addDimensionValue(String key, String value) {
        if ( key != null && key.trim().length() > 0 ) {
            if ( value != null ) {
                dimensionValues.put(key, value);
            } else {
                dimensionValues.put(key, NULL_VALUE_PLACEHOLDER);
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        return dimensionValues != null ? dimensionValues.equals(sample.dimensionValues) : sample.dimensionValues == null;

    }

    @Override
    public int hashCode() {
        return dimensionValues != null ? dimensionValues.hashCode() : 0;
    }

    public Sample narrow( Set<String> subKeys ) {
        Sample sample = new Sample();
        subKeys.forEach(s -> {
            sample.addDimensionValue(s, dimensionValues.get(s));
        });

        return sample;
    }

    public TreeMap<String, String> getDimensionValues() {
        return dimensionValues;
    }

    public void setDimensionValues(TreeMap<String, String> dimensionValues) {
        this.dimensionValues = dimensionValues;
    }
}
