package org.ffpy.plugin.coding.action.env;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ActionEnvProxy implements InvocationHandler {

    private final ActionEnv target;
    private final Map<String, Object> cacheMap = new HashMap<>();

    public static ActionEnv getInstance(ActionEnv target) {
        return (ActionEnv) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), new ActionEnvProxy(target));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isCache = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes())
                .getAnnotation(Cache.class) != null;
        if (isCache) {
            String key = method.getName();
            Object value = cacheMap.get(key);
            if (value == null) {
                value = method.invoke(target, args);
                if (value != null) {
                    cacheMap.put(key, value);
                }
            }
            return value;
        }

        return method.invoke(target, args);
    }
}
