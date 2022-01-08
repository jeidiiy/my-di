package com.wscrg;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.wscrg.StringUtils.lowerCaseFirst;

public class BeanFactory {

    public Map<String, Object> createBeanByConfiguration(Class<?> instance) {

        Map<String, Object> beanMap = new ConcurrentHashMap<>();

        Arrays.stream(instance.getDeclaredMethods()).forEach(m -> {
            if (m.getAnnotation(Bean.class) != null) {
                m.setAccessible(true);
                Class<?> returnType = m.getReturnType();

                try {
                    Object invoke = m.invoke(instance.getConstructor().newInstance());

                    String key = lowerCaseFirst(returnType.getSimpleName());
                    beanMap.put(key, invoke);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return beanMap;
    }

    public Map<String, Object> createBeanByComponentScan(Set<Class<?>> annotatedBeanList) {

        String key;
        Map<String, Object> beanMap = new ConcurrentHashMap<>();

        for (Class<?> annotatedBean : annotatedBeanList) {

            if (annotatedBean.isAnnotation() || annotatedBean.isInterface())
                continue;

            key = lowerCaseFirst(annotatedBean.getSimpleName());

            try {
                beanMap.put(key, annotatedBean.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return beanMap;
    }
}
