package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.utils.Constants;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tuwenjie on 2016/11/15.
 */
public class Counter {

    //被拦截的个数，无需绝对正确，不需要同步
    private static volatile long rejectCount;

    //放行的个数，无需绝对正确，不需要同步
    private static volatile long passCount;

    public static void incrRejectCount( ) {
        rejectCount++;
        //溢出，重新计数
        if ( rejectCount < 0) {
            rejectCount = 0;
        }
    }

    public static void incrPassCount( ) {
        passCount++;

        //溢出，重新计数
        if ( passCount < 0) {
            passCount = 0;
        }
    }

    public static void log( ) {
        Constants.ACCESS_LOGGER.info("rejectCount:{}; passCount:{}", rejectCount, passCount);
    }
}
