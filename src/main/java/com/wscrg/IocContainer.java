package com.wscrg;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

public class IocContainer {

    private static Map<String, Object> beanMap = new ConcurrentHashMap<>();

    public IocContainer(Class<?> appConfig) {
        getObject(appConfig);
    }

    public static <T> void getObject(Class<T> classType) {

        Annotation[] declaredAnnotations = classType.getDeclaredAnnotations();

        for (Annotation annotation : declaredAnnotations) {
            String annotationName = annotation.annotationType().getSimpleName();
            if (annotationName.equals("ComponentScan")) {
                prepareBean(classType.getPackageName());
            }
        }
    }

    /**
     * @ComponentScan이 선언된 자바 파일의 위치부터 모든 하위 패키지를 탐색하며
     * @Component 어노테이션이 선언된 클래스를 탐색하고 등록한다.
     **/
    private static void prepareBean(String packageName) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> annotatedBeanList = reflections.getTypesAnnotatedWith(Component.class);
        String key = null;

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

    }

    private static String lowerCaseFirst(String str) {
        char[] arr = str.toCharArray();
        arr[0] = Character.toLowerCase(arr[0]);
        return new String(arr);
    }

    public Set<String> getBeans() {
        return beanMap.keySet();
    }
}
