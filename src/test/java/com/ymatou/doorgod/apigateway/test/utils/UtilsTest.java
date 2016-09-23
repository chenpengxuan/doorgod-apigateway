package com.ymatou.doorgod.apigateway.test.utils;

import com.ymatou.doorgod.apigateway.utils.Utils;
import org.junit.Test;

import java.util.TreeSet;

/**
 * Created by tuwenjie on 2016/9/13.
 */
public class UtilsTest {

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
}
