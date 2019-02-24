package com.example.serviceproxy;

import android.app.Application;

import com.xuwei.serviceproxy.ServiceProxy;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://easy-mock.com/mock/5c511379d858826be92e5b8d/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ServiceProxy.init(retrofit);
    }
}
