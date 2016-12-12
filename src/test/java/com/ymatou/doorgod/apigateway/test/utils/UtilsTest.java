package com.ymatou.doorgod.apigateway.test.utils;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.model.StatisticItem;
import com.ymatou.doorgod.apigateway.utils.Utils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Created by tuwenjie on 2016/9/13.
 */
public class UtilsTest {

    @Test
    public void testJson( ) {

        System.out.println( JSON.toJSONString(null));

        Sample sample = new Sample();
        sample.addDimensionValue("ip", "129.19.90.99");

        String json = JSON.toJSONString(sample);
        System.out.println( json );

        StatisticItem item = new StatisticItem();
        item.setSample(JSON.toJSONString(sample));

        System.out.println( JSON.toJSONString(item));

        long milliSeconds = System.currentTimeMillis();
        System.out.println( Utils.getCurrentTimeStr());


        Set<String> names = new HashSet<String>();
        names.add("100");
        names.add("200");

        json = JSON.toJSONString(names);

        System.out.println( "set json:" + json);

        names = JSON.parseObject(json, Set.class);

        System.out.println( names );

        long maxPlus1 = Long.MAX_VALUE + 1;

        System.out.println(Pattern.matches("10\\.11\\..*", "10.11.30.13"));

        System.out.println( maxPlus1);

    }

    @Test
    public void testSplitByComma() {
        System.out.println( Utils.splitByComma(null));
        System.out.println( Utils.splitByComma(""));
        System.out.println( Utils.splitByComma("    eee,  erwe,"));
        System.out.println( Utils.splitByComma("eee,erwe,"));
        System.out.println( Utils.splitByComma("eee,erwe"));

    }

    @Test
    public void test( ) {
       System.out.println(Utils.localIp());
    }

    @Test
    public void testTreeSet( ) {
        TreeSet<Entity> set = new TreeSet<Entity>((o1, o2)->{
            return 0;
        });
        set.add(new Entity());
        set.add(new Entity());

        System.out.println( set.size());

    }

    public static class Entity  {
    }

    @Test
    public void testRegularExp( ) {
        System.out.println(Pattern.matches("/ab/|/yid/", "/ab/"));
        System.out.println(Pattern.matches("/ab/|/yid/", "/yid/"));
    }
}
