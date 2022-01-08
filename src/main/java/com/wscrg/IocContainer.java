package com.wscrg;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.wscrg.StringUtils.lowerCaseFirst;

public class IocContainer {

    private final static Map<String, Object> beanMap = new ConcurrentHashMap<>();
    private final BeanFactory beanFactory = new BeanFactory();

    public IocContainer(Class<?> appConfig) {
        registerBeans(appConfig);
        injectBeans();
    }

    public <T> void registerBeans(Class<T> classType) {

        if (classType == null) {
            throw new IllegalArgumentException("입력된 인스턴스가 null입니다.");
        }

        Annotation[] declaredAnnotations = classType.getDeclaredAnnotations();

        for (Annotation annotation : declaredAnnotations) {
            String annotationName = annotation.annotationType().getSimpleName();
            if (annotationName.equals("ComponentScan")) {
                registerBeanByComponentScan(classType.getPackageName());
            }

            if (annotationName.equals("Configuration")) {
                registerBeanByConfiguration(classType.getPackageName() + "." + classType.getSimpleName());
            }
        }
    }

    /**
     * @Configuration 파일에 작성된 @Bean으로 작성한 메서드 등록
     */
    private void registerBeanByConfiguration(String packageName) {

        Class<?> instance;

        try {
            instance = Class.forName(packageName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> beanByConfiguration = beanFactory.createBeanByConfiguration(instance);
        beanMap.putAll(beanByConfiguration);

    }

    /**
     * @ComponentScan이 선언된 자바 파일의 위치부터 모든 하위 패키지를 탐색하며
     * @Component 어노테이션이 선언된 클래스를 탐색하고 등록한다.
     **/
    private void registerBeanByComponentScan(String packageName) {

        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> annotatedBeanSet = reflections.getTypesAnnotatedWith(Component.class);

        Map<String, Object> beanByComponentScan = beanFactory.createBeanByComponentScan(annotatedBeanSet);
        beanMap.putAll(beanByComponentScan);

    }

    /**
     * @Injectable이 선언된 필드를 찾고 해당 빈을 주입한다.
     **/
    private void injectBeans() {
        Collection<?> beans = beanMap.values();

        injectBeanInConstructor(beans);
        injectBeanInMethod(beans);
        injectBeanInField(beans);
    }

    /**
     * 컨테이너에 등록된 빈 중 @Injectable이 선언된 필드의 타입에 해당하는 빈을 주입한다.
     *
     * @param beans 초기화가 완료된 빈 컬렉션
     * @throws BeanNotFoundException 해당하는 타입의 빈을 찾지 못하면 발생한다.
     */
    private void injectBeanInField(Collection<?> beans) {
        beans.forEach(bean -> Arrays.stream(bean.getClass().getDeclaredFields()).collect(Collectors.toSet())
                .forEach(field -> {
                    if (field.getAnnotation(Injectable.class) != null) {
                        Object foundedBean = beanMap.get(field.getName());
                        if (foundedBean == null) {
                            throw new BeanNotFoundException(String.format("Bean cannot be founded: %s", field.getType().getSimpleName()));
                        }
                        field.setAccessible(true);
                        try {
                            field.set(bean, foundedBean);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }));
    }

    /**
     * 컨테이너에 등록된 빈 중 @Injectable이 선언된 메서드의 파라미터 타입에 해당하는 빈을 주입한다.
     *
     * @param beans 초기화가 완료된 빈 컬렉션
     * @throws BeanNotFoundException 해당하는 타입의 빈을 찾지 못하면 발생한다.
     */
    private void injectBeanInMethod(Collection<?> beans) {
        beans.forEach(bean -> Arrays.stream(bean.getClass().getDeclaredMethods()).collect(Collectors.toSet())
                .forEach(method -> {
                    if (method.getAnnotation(Injectable.class) != null) {
                        Parameter[] parameters = method.getParameters();
                        Arrays.stream(parameters).forEach(param -> {
                            Object foundedBean = beanMap.get(lowerCaseFirst(param.getType().getSimpleName()));
                            if (foundedBean == null) {
                                throw new BeanNotFoundException(String.format("Bean cannot be founded: %s", param.getType().getSimpleName()));
                            }
                            Field foundField = Arrays.stream(bean.getClass().getDeclaredFields()).filter(field ->
                                    field.getType().getSimpleName().equals(param.getType().getSimpleName())
                            ).findFirst().orElseThrow(() -> new BeanNotFoundException(String.format("Bean cannot be founded: %s", param.getType().getSimpleName())));

                            foundField.setAccessible(true);
                            try {
                                foundField.set(bean, foundedBean);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }));
    }

    /**
     * 컨테이너에 등록된 빈 중 @Injectable이 선언된 생성자의 파라미터 타입에 해당하는 빈을 주입한다.
     *
     * @param beans 초기화가 완료된 빈 컬렉션
     * @throws BeanNotFoundException 해당하는 타입의 빈을 찾지 못하면 발생한다.
     */
    private void injectBeanInConstructor(Collection<?> beans) {
        beans.forEach(bean -> Arrays.stream(bean.getClass().getDeclaredConstructors()).collect(Collectors.toSet())
                .forEach(constructor -> {
                    if (constructor.getAnnotation(Injectable.class) != null) {
                        Parameter[] parameters = constructor.getParameters();
                        Arrays.stream(parameters).forEach(param -> {
                            Object foundedBean = beanMap.get(lowerCaseFirst(param.getType().getSimpleName()));
                            if (foundedBean == null) {
                                throw new BeanNotFoundException(String.format("Bean cannot be founded: %s", param.getType().getSimpleName()));
                            }
                            Field foundField = Arrays.stream(bean.getClass().getDeclaredFields()).filter(field ->
                                    field.getType().getSimpleName().equals(param.getType().getSimpleName())
                            ).findFirst().orElseThrow(() -> new BeanNotFoundException(String.format("Bean cannot be founded: %s", param.getType().getSimpleName())));

                            foundField.setAccessible(true);
                            try {
                                foundField.set(bean, foundedBean);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }));
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
