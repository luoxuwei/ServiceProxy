package com.example.serviceproxy.net;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface DemoApi {
    @GET("/mock/5c511379d858826be92e5b8d/example/service_test")
    Observable<UserInfo> getUserInfo();

    @GET("/mock/5c511379d858826be92e5b8d/example/service_test1")
    Observable<UserInfo> getUserInfo1();

    @GET("/mock/5c511379d858826be92e5b8d/example/service_test2")
    Observable<UserInfo> getUserInfo2();
}
