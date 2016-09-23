package com.ymatou.doorgod.apigateway.test;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tuwenjie on 2016/9/20.
 */
public class HystrixTest {

    public static class MyObservableCommand extends HystrixObservableCommand {

        public MyObservableCommand(HystrixCommandGroupKey group) {
            super(group);
        }

        @Override
        protected Observable construct() {
            return Observable.create(new Observable.OnSubscribe<Void>() {
                @Override
                public void call(Subscriber<? super Void> subscriber) {
                    try {
                        if (!subscriber.isUnsubscribed()) {
                            Thread.sleep(800);
                            System.out.println("Finished command");
                            subscriber.onCompleted();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            } );
        }
    }

    @Test
    public void test( ) {
        MyObservableCommand cmd = new MyObservableCommand(HystrixCommandGroupKey.Factory.asKey("test"));
        cmd.observe();
        System.out.println("Finished observe");

        cmd = new MyObservableCommand(HystrixCommandGroupKey.Factory.asKey("test"));
        cmd.toObservable().subscribe((Void)->{}, (e)->{}, ()->{System.out.println("Finished observe");});

        System.out.println("end");


    }


}
