package com.example.serviceproxy

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.serviceproxy.net.DemoService
import com.xuwei.serviceproxy.ServiceProxy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.test_service).setOnClickListener({
            ServiceProxy.of(DemoService::class.java)
                    .getUserName("sss")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this@MainActivity, "用户名是："+it, Toast.LENGTH_LONG).show()
                    }, {
                        Toast.makeText(this@MainActivity, "测试失败,请检查网络情况", Toast.LENGTH_LONG).show()
                    })
        })
        findViewById<View>(R.id.test_service1).setOnClickListener({
            ServiceProxy.of(DemoService::class.java)
                    .getUserName1("sss")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this@MainActivity, "用户名是："+it, Toast.LENGTH_LONG).show()
                    }, {
                        Toast.makeText(this@MainActivity, "测试失败,请检查网络情况", Toast.LENGTH_LONG).show()
                    })
        })
        findViewById<View>(R.id.test_service2).setOnClickListener({
            ServiceProxy.of(DemoService::class.java)
                    .getUserName2("sss")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this@MainActivity, "用户名是："+it, Toast.LENGTH_LONG).show()
                    }, {
                        Toast.makeText(this@MainActivity, "测试失败,请检查网络情况", Toast.LENGTH_LONG).show()
                    })
        })

    }

}
