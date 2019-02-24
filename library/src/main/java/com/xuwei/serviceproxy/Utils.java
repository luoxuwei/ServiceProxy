package com.xuwei.serviceproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Utils {
    public static Object invoke(int index, Object[] args, Object obj, Method[] methods, InvocationHandler invocationHandler) {
        Method method = methods[index];
        if (invocationHandler != null) {
            try {
                return invocationHandler.invoke(obj, method, args);
            } catch (Throwable th) {
                RuntimeException runtimeException = new RuntimeException(th);
                throw runtimeException;
            }
        } else {
            throw new IllegalStateException();
        }
    }
}
