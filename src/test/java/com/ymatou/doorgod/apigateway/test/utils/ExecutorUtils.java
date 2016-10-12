/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.apigateway.test.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author luoshiqian 2016/10/8 16:37
 */
public class ExecutorUtils {

    public static ExecutorService newExecutors(int nums){
        return Executors.newFixedThreadPool(nums);
    }

}
