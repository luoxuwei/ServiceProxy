# ServiceProxy M层抽象框架
---
这个项目实现了微信读书技术博客介绍的基于AOP的M层抽象的方案，文章地址：[基于 AOP 的 M 层抽象](https://medium.com/weread/%E5%9F%BA%E4%BA%8E-aop-%E7%9A%84-m-%E5%B1%82%E6%8A%BD%E8%B1%A1-fd4fd144c0d4)

### 接入ServiceProxy
```groovy
implementation 'com.github.luoxuwei.ServiceProxy:library:1.0.1'
implementation 'com.github.luoxuwei.ServiceProxy:annotation:1.0.1'
annotationProcessor 'com.github.luoxuwei.ServiceProxy:annotation-compiler:1.0.1'
```
### 使用指南

1.定义业务模块用到的后端api的Retrofit接口

```java
public interface DemoApi {
    @GET("https://easy-mock.com/mock/5c511379d858826be92e5b8d/example/service_test")
    Observable<UserInfo> getUserInfo();

    @GET("https://easy-mock.com/mock/5c511379d858826be92e5b8d/example/service_test1")
    Observable<UserInfo> getUserInfo1();

    @GET("https://easy-mock.com/mock/5c511379d858826be92e5b8d/example/service_test2")
    Observable<UserInfo> getUserInfo2();
}
```
2.定义封装业务逻辑的模块类，继承定义好的Retrofit接口，并添加ProxyModule注解

```java
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

    public Observable<String> getUserName1(String id) {
        Observable<String> res = Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getUserInfo1().blockingFirst().nick;
            }
        });
        return res;
    }

    public Observable<String> getUserName2(String id) {
        Observable<String> res = Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getUserInfo2().blockingFirst().nick;
            }
        });
        return res;
    }
}
```
因为继承了Retrofit接口，又不能实现接口中的方法，类必须声明为abstract。

3.在使用了ServiceProxy的项目的build.gradle文件里加上注解处理器的配置.

```groovy
annotationProcessor 'com.github.luoxuwei.ServiceProxy:annotation-compiler:1.0.1'
```