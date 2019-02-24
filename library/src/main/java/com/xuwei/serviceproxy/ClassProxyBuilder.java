package com.xuwei.serviceproxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.reflect.Modifier.STATIC;


/**
 * Created by xuwei.luo on 18/8/31.
 */
//https://github.com/linkedin/dexmaker/blob/master/dexmaker/src/main/java/com/android/dx/stock/ProxyBuilder.java
public final class ClassProxyBuilder<T> {
    private static final String FIELD_NAME_HANDLER = "$__handler";
    private static final String FIELD_NAME_METHODS = "$__methodArray";
    private static final String PROXY_CLASS_NAME_MODEL = "%s_proxy";
    private static final String PROXY_CLASS_PACKAGE = "com.xuwei.serviceproxy";
    private static final ClassLoader sParentClassLoader = ClassProxyBuilder.class.getClassLoader();
    private static Map<MethodKey, Method> sSuperMethodMaps = new HashMap();
    private final Class<T> baseClass;

    private static final class MethodKey {
        final Class<?> clz;
        final Method method;

        MethodKey(Class<?> cls, Method method) {
            this.clz = cls;
            this.method = method;
        }

        public final int hashCode() {
            return this.method.hashCode();
        }

        public final boolean equals(Object other) {
            if (!(other instanceof MethodKey)) {
                return false;
            }
            MethodKey methodKey = (MethodKey) other;
            if (this.method.equals(methodKey.method)) {
                return this.clz.equals(methodKey.clz);
            }
            return false;
        }
    }

    /**
     * Wrapper class to let us disambiguate {@link Method} objects.
     * <p>
     * The purpose of this class is to override the {@link #equals(Object)} and {@link #hashCode()}
     * methods so we can use a {@link Set} to remove duplicate methods that are overrides of one
     * another. For these purposes, we consider two methods to be equal if they have the same
     * name, return type, and parameter types.
     */
    private static class MethodSetEntry implements Comparable<MethodSetEntry> {
        private final String name;
        private final Method originalMethod;
        private final Class<?>[] paramTypes;
        private String paramsString;
        private final Class<?> returnType;

        public MethodSetEntry(Method method) {
            this.originalMethod = method;
            this.name = method.getName();
            this.paramTypes = method.getParameterTypes();
            this.returnType = method.getReturnType();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof MethodSetEntry)) {
                return false;
            }
            MethodSetEntry methodSetEntry = (MethodSetEntry) other;
            if (this.name.equals(methodSetEntry.name) && Arrays.equals(this.paramTypes, methodSetEntry.paramTypes)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hashCode = (this.name.hashCode() + 527) + 17;
            return hashCode + ((hashCode * 31) + Arrays.hashCode(this.paramTypes));
        }

        private String getParamsString() {
            if (this.paramsString == null) {
                this.paramsString = separateWithCommas(this.paramTypes);
            }
            return this.paramsString;
        }

        @Override
        public int compareTo(MethodSetEntry methodSetEntry) {
            int compareTo = this.name.compareTo(methodSetEntry.name);
            return compareTo != 0 ? compareTo : getParamsString().compareTo(methodSetEntry.getParamsString());
        }

        String separateWithCommas(Class<?>[] clsArr) {
            int i = 0;
            if (clsArr == null || clsArr.length <= 0) {
                return "";
            }
            if (clsArr.length == 1) {
                return clsArr[0].getName();
            }
            StringBuilder stringBuilder = new StringBuilder();
            while (i < clsArr.length) {
                stringBuilder.append(clsArr[i].getName());
                if (i < clsArr.length - 1) {
                    stringBuilder.append(",");
                }
                i++;
            }
            return stringBuilder.toString();
        }
    }

    private ClassProxyBuilder(Class<T> cls) {
        this.baseClass = cls;
    }

    public static <T> ClassProxyBuilder<T> forClass(Class<T> cls) {
        return new ClassProxyBuilder(cls);
    }

    public final Class<? extends T> buildProxyClass() throws ClassNotFoundException {
        Method[] methodsToProxyRecursive;
        Class<? extends T> proxyClass = (Class<? extends T>) sParentClassLoader.loadClass(PROXY_CLASS_PACKAGE+"." + String.format(PROXY_CLASS_NAME_MODEL, new Object[]{this.baseClass.getSimpleName()}));

        methodsToProxyRecursive = getMethodsToProxyRecursive(this.baseClass);

        setMethodsStaticField(proxyClass, methodsToProxyRecursive);
        return proxyClass;
    }

    private static void setMethodsStaticField(Class<?> proxyClass, Method[] methodsToProxy) {
        try {
            Field methodArrayField = proxyClass.getDeclaredField(FIELD_NAME_METHODS);
            methodArrayField.setAccessible(true);
            methodArrayField.set(null, methodsToProxy);
        } catch (NoSuchFieldException e) {
            // Should not be thrown, generated proxy class has been generated with this field.
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, we just set the field to accessible.
            throw new AssertionError(e);
        }
    }

    public static void setInvocationHandler(Object instance, InvocationHandler handler) {
        try {
            Field handlerField = instance.getClass().getDeclaredField(FIELD_NAME_HANDLER);
            handlerField.setAccessible(true);
            handlerField.set(instance, handler);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Not a valid proxy instance", e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, we just set the field to accessible.
            throw new AssertionError(e);
        }
    }

    // TODO: test coverage for isProxyClass

    /**
     * Returns true if {@code c} is a proxy class created by this builder.
     */
    public static boolean isProxyClass(Class<?> c) {
        // TODO: use a marker interface instead?
        try {
            c.getDeclaredField(FIELD_NAME_HANDLER);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * Returns the proxy's {@link InvocationHandler}.
     *
     * @throws IllegalArgumentException if the object supplied is not a proxy created by this class.
     */
    public static InvocationHandler getInvocationHandler(Object instance) {
        try {
            Field field = instance.getClass().getDeclaredField(FIELD_NAME_HANDLER);
            field.setAccessible(true);
            return (InvocationHandler) field.get(instance);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Not a valid proxy instance", e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, we just set the field to accessible.
            throw new AssertionError(e);
        }
    }

    public static Object callSuper(Object obj, Method method, Object... args) throws Throwable {
        try {
            MethodKey methodKey = new MethodKey(obj.getClass(), method);
            Method callSuperMethod = (Method) sSuperMethodMaps.get(methodKey);
            if (callSuperMethod == null) {
                callSuperMethod = obj.getClass().getMethod(superMethodName(method), method.getParameterTypes());
                sSuperMethodMaps.put(methodKey, callSuperMethod);
            }
            return callSuperMethod.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * The super method must include the return type, otherwise its ambiguous
     * for methods with covariant return types.
     */
    private static String superMethodName(Method method) {
        return "super$" + method.getName() + "$" + method.getReturnType().getName().replace('.', '_').replace('[', '_').replace(';', '_');
    }

    private Method[] getMethodsToProxyRecursive(Class<?> cls) {
        Set<MethodSetEntry> methodsToProxy = new HashSet<>();
        Set<MethodSetEntry> seenFinalMethods = new HashSet<>();
        for (Class c = cls; c != null; c = c.getSuperclass()) {
            getMethodsToProxy(methodsToProxy, seenFinalMethods, c);
        }

        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (Class<?> iface : c.getInterfaces()) {
                getMethodsToProxy(methodsToProxy, seenFinalMethods, iface);
            }
        }

        MethodSetEntry[] methodSetEntryArr = new MethodSetEntry[methodsToProxy.size()];
        int i=0;
        for (MethodSetEntry methodSetEntry:methodsToProxy) {
            methodSetEntryArr[i] = methodSetEntry;
            i++;
        }
        Arrays.sort(methodSetEntryArr);

        Method[] methodArr = new Method[methodsToProxy.size()];
        i = 0;
        while (i < methodSetEntryArr.length) {
            methodArr[i] = methodSetEntryArr[i].originalMethod;
            i++;
        }
        return methodArr;
    }

    private void getMethodsToProxy(Set<MethodSetEntry> sink, Set<MethodSetEntry> seenFinalMethods,
                                   Class<?> c) {
        for (Method method : c.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.FINAL) != 0) {
                // Skip final methods, we can't override them. We
                // also need to remember them, in case the same
                // method exists in a parent class.
                MethodSetEntry entry = new MethodSetEntry(method);
                seenFinalMethods.add(entry);
                // We may have seen this method already, from an interface
                // implemented by a child class. We need to remove it here.
                sink.remove(entry);
                continue;
            }

            // Skip static methods, overriding them has no effect.

            // Skip private methods, since they are invoked through direct
            // invocation (as opposed to virtual). Therefore, it would not
            // be possible to intercept any private method defined inside
            // the proxy class except through reflection.

            // Skip package-private methods as well (for non-shared class
            // loaders). The proxy class does
            // not actually inherit package-private methods from the parent
            // class because it is not a member of the parent's package.
            // This is even true if the two classes have the same package
            // name, as they use different class loaders.

            // Skip finalize method, it's likely important that it execute as normal.


            if ((method.getModifiers() & STATIC) == 0
                    && ((Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) && !(method.getName().equals("finalize") && method.getParameterTypes().length == 0))) {

                MethodSetEntry methodSetEntry;
                methodSetEntry = new MethodSetEntry(method);
                if (!(seenFinalMethods.contains(methodSetEntry) || sink.contains(methodSetEntry))) {
                    sink.add(methodSetEntry);
                }
                continue;
            }
        }
    }

    private static Set<MethodSetEntry> getFinalMethods(Class<?> cls) {
        Set<MethodSetEntry> hashSet = new HashSet();
        for (Method method : cls.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.FINAL) != 0) {
                hashSet.add(new MethodSetEntry(method));
            }
        }
        return hashSet;
    }
}
