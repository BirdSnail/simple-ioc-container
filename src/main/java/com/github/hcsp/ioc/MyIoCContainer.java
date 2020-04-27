package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MyIoCContainer {

    private Map<String, Object> beans = new HashMap<>();

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException("properties路径有误", e);
        }

        properties.forEach((beanName, beanInstance) -> {
            try {
                Class<?> klass = Class.forName((String) beanInstance);
                final Object bean = klass.newInstance();
                beans.put((String) beanName, bean);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        });

        beans.forEach((beanName, beanInstance) -> injectDependency(beanInstance, beans));
    }


    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    private void injectDependency(Object bean, Map<String, Object> allBeans) {
        List<Field> fieldList = Arrays.stream(bean.getClass().getDeclaredFields())
                .filter(filed -> filed.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fieldList.forEach(field -> {
            try {
                String fieldName = field.getName();
                field.setAccessible(true);
                field.set(bean, allBeans.get(fieldName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

}
