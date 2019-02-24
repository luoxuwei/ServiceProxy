package com.xuwei.serviceproxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;


/**
 * Created by xuwei.luo on 18/8/29.
 */
//根据微信阅读官方博客的描述实现一套M层的抽象
//https://medium.com/weread/%E5%9F%BA%E4%BA%8E-aop-%E7%9A%84-m-%E5%B1%82%E6%8A%BD%E8%B1%A1-fd4fd144c0d4
public class ServiceProxy {

    private Retrofit mRetrofit;

    private ConcurrentHashMap<Class<?>, Object> mServiceMap = new ConcurrentHashMap();

    public static void init(Retrofit retrofit) {
        SingleHolder.instance.mRetrofit = retrofit;
    }

    public static <T> T of(Class<T> cls) {
        if (SingleHolder.instance.mRetrofit == null) {
            throw new RuntimeException("no retrofit!");
        }
        return SingleHolder.instance.getService(cls);
    }

    private <T> T getService(Class<T> cls) {
        if (!this.mServiceMap.containsKey(cls)) {
            try {
                this.mServiceMap.putIfAbsent(cls, create(cls));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return (T) this.mServiceMap.get(cls);
    }

    private <T> T create(Class<T> cls) {
        if (cls.isInterface() && cls.getInterfaces().length == 0) {
            return mRetrofit.create(cls);
        }
        final HashSet allInterfaces = Reflections.getAllInterfaces(cls);
        return Reflections.proxy(cls, new InvocationHandler() {
            //如果方法在接口（而不是抽象类）定义，则转发给 retrofit，如果方法在抽象类里定义，则假定一定有实现代码（而不是一个抽象方法），
            // 可以直接调用，否则抛出异常。
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Iterator it = allInterfaces.iterator();
                while (it.hasNext()) {
                    Method declaredMethod;
                    Class interfaceCls = (Class) it.next();
                    try {
                        declaredMethod = interfaceCls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        declaredMethod = null;
                    }
                    if (declaredMethod != null) {
                        if (interfaceCls.getInterfaces().length != 0) {
                            // 对于实现多个接口的情况，仅允许没有继承的接口调用这个方法
                            // 参考 {@link retrofit.Utils#validateServiceClass(Class)}
                            ClassProxyBuilder.callSuper(proxy, method, args);
                            continue;
                        }
                        try {
                            return method.invoke(ServiceProxy.this.getService(interfaceCls), args);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return ClassProxyBuilder.callSuper(proxy, method, args);
            }
        }, new Object[0]);

    }

    private static class SingleHolder {
        private static ServiceProxy instance = new ServiceProxy();
    }
}
