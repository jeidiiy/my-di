package com.wscrg;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IocContainer {

    private final static Map<String, Object> beanMap = new ConcurrentHashMap<>();

    public IocContainer(Class<?> appConfig) {
        getObject(appConfig);
        injectBean();
    }

    public static <T> void getObject(Class<T> classType) {

        Annotation[] declaredAnnotations = classType.getDeclaredAnnotations();

        for (Annotation annotation : declaredAnnotations) {
            String annotationName = annotation.annotationType().getSimpleName();
            if (annotationName.equals("ComponentScan")) {
                prepareBean(classType.getPackageName());
            }

            if (annotationName.equals("Configuration")) {
                StringBuffer sb = new StringBuffer();
                sb.append(classType.getPackageName()).append(".").append(classType.getSimpleName());
                prepareMethodBean(sb.toString());
            }
        }
    }

    /**
     * @Configuration 파일에 작성된 @Bean으로 작성한 메서드 등록
     */
    private static void prepareMethodBean(String packageName) {

        Class<?> instance;

        try {
            instance = Class.forName(packageName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

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

    /**
     * @Injectable이 선언된 필드를 찾고 해당 빈을 주입한다.
     **/
    private static void injectBean() {
        // 필드 주입
        Collection<Object> beans = beanMap.values();
        beans.forEach(bean -> {
            Arrays.stream(bean.getClass().getDeclaredFields()).collect(Collectors.toSet())
                    .forEach(field -> {
                        if (field.getAnnotation(Injectable.class) != null) {
                            Object o = beanMap.get(field.getName());
                            field.setAccessible(true);
                            try {
                                field.set(bean, o);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        });
    }

    private static String lowerCaseFirst(String str) {
        char[] arr = str.toCharArray();
        arr[0] = Character.toLowerCase(arr[0]);
        return new String(arr);
    }

    public Set<String> getBeans() {
        return beanMap.keySet();
    }

    public Object getBean(String key) {
        return beanMap.get(key);
    }

    public Collection<?> getBeansInstance() {
        return beanMap.values();
    }
}
