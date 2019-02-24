package com.xuwei.serviceproxy;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;


public class Reflections {
    private static final Field ifTable;

    public static <T> HashSet<Class<? extends T>> filterSuperBy(Class<T> superCls, Class<? extends T> targetCls) {
        if (superCls.isInterface()) {
            HashSet allInterfaces = getAllInterfaces(targetCls);
            allInterfaces.remove(superCls);
            HashSet<Class<? extends T>> hashSet = new HashSet();
            Iterator it = allInterfaces.iterator();
            while (it.hasNext()) {
                Class clz = (Class) it.next();
                if (isExtendsFrom(superCls, clz)) {
                    hashSet.add(clz);
                }
            }
            return hashSet;
        }
        throw new IllegalArgumentException("base should be a interface");
    }

    static {
        Field field = null;
        try {
            field = Class.class.getDeclaredField("ifTable");
        } catch (Exception e) {
        }
        ifTable = field;
    }

    public static HashSet<Class<?>> getAllInterfaces(Class<?> cls) {
        HashSet<Class<?>> hashSet = new HashSet();
        if (ifTable != null) {
            ifTable.setAccessible(true);
            try {
                Object[] objArr = (Object[]) ifTable.get(cls);
                if (objArr != null) {
                    for (Object obj : objArr) {
                        if (obj instanceof Class) {
                            hashSet.add((Class) obj);
                        }
                    }
                }
                return hashSet;
            } catch (IllegalAccessException e) {
            }
        }
        do {
            Class<?>[] interfaces = cls.getInterfaces();
            if (interfaces.length > 0) {
                for (Class cls2 : interfaces) {
                    if (!hashSet.contains(cls2)) {
                        hashSet.addAll(getAllInterfaces(cls2));
                    }
                }
                hashSet.addAll(Arrays.asList(interfaces));
            }
            cls = cls.getSuperclass();
            if (cls == null) {
                break;
            }
        } while (cls != Object.class);
        return hashSet;
    }

    public static boolean isExtendsFrom(Class<?> from, Class<?> target) {
        if (from == target) {
            return true;
        }
        for (Class isExtendsFrom : target.getInterfaces()) {
            if (isExtendsFrom(from, isExtendsFrom)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T proxy(Class<T> cls, InvocationHandler invocationHandler, Object... objconstructorArgValuesArr) {
        if (!cls.isInterface()) {
            return proxyClass(cls, invocationHandler, objconstructorArgValuesArr);
        }

        return cls.cast(Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, invocationHandler));
    }

    private static <T> T proxyClass(Class<T> cls, InvocationHandler invocationHandler, Object... objconstructorArgValuesArr) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            Constructor[] declaredConstructors = createProxyClass(cls).getDeclaredConstructors();
            if (declaredConstructors == null || declaredConstructors.length == 0) {
                throw new RuntimeException("No declared constructor available:" + cls.getName());
            }
            Constructor targetConstructor = null;
            for (Constructor constructor : declaredConstructors) {
                Class[] parameterTypes = constructor.getParameterTypes();
                int length = objconstructorArgValuesArr == null ? 0 : objconstructorArgValuesArr.length;
                if (length == (parameterTypes == null ? 0 : parameterTypes.length)) {
                    if (length != 0) {
                        int i = 0;
                        while (i < parameterTypes.length) {
                            Class parameterType = parameterTypes[i];
                            if (parameterType != objconstructorArgValuesArr[i].getClass()) {
                                if (!parameterType.isPrimitive() || Primitives.wrap(parameterType) != objconstructorArgValuesArr[i].getClass()) {
                                    break;
                                }
                            }
                            i++;
                        }
                        if (i == parameterTypes.length) {
                            targetConstructor =  constructor;
                            break;
                        }
                    } else {
                        targetConstructor =  constructor;
                        break;
                    }
                }
            }

            if (targetConstructor == null) {
                throw new RuntimeException("No available constructor for proxy found:" + cls.getName());
            }
            targetConstructor.setAccessible(true);
            T newInstance = (T) targetConstructor.newInstance(objconstructorArgValuesArr);
            ClassProxyBuilder.setInvocationHandler(newInstance, invocationHandler);
            System.out.println("Create proxy class:" + cls.getSimpleName() + " consumed:" + (System.currentTimeMillis() - currentTimeMillis));
            return newInstance;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Class<? extends T> createProxyClass(Class<T> cls) {
        try {
            return ClassProxyBuilder.forClass(cls).buildProxyClass();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
