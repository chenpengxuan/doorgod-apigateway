package com.ymatou.doorgod.apigateway.utils;

import org.junit.Test;

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
}
