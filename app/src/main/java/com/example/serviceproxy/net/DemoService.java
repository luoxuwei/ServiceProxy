package com.example.serviceproxy.net;

import com.xuwei.serviceproxy.annotation.ProxyModule;

import java.util.concurrent.Callable;

import io.reactivex.Observable;

@ProxyModule
public abstract class DemoService implements DemoApi {

    public Observable<String> getUserName(String id) {
        Observable<String> res = Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getUserInfo().blockingFirst().nick;
            }
        });
        return res;
    }
}
