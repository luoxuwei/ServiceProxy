package com.example.serviceproxy.net;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface DemoApi {
    @GET("https://easy-mock.com/mock/5c511379d858826be92e5b8d/example/service_test")
    Observable<UserInfo> getUserInfo();
}
