package com.example.serviceproxy;

import android.app.Application;
import android.text.TextUtils;

import com.xuwei.serviceproxy.ServiceProxy;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceProxy.init(baseUrl -> {
            if (TextUtils.isEmpty(baseUrl)) {
                baseUrl = "https://easy-mock.com";
            }
            return new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        });
    }
}
