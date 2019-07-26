package com.example.serviceproxy.net;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface DemoApi1 {
    @GET("/mock/5d39931653369c7ed6ec87fc/example_copy/service_test")
    Observable<UserInfo> getUserInfo();

    @GET("/mock/5d39931653369c7ed6ec87fc/example_copy/service_test1")
    Observable<UserInfo> getUserInfo1();

    @GET("/mock/5d39931653369c7ed6ec87fc/example_copy/service_test2")
    Observable<UserInfo> getUserInfo2();
}
