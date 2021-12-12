package com.wscrg;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class IocContainerTest {

    @Test
    public void registerBeanTest() {
        IocContainer iocContainer = new IocContainer(AppConfig.class);

        Set<String> beans = iocContainer.getBeans();
        assertEquals(4, beans.size());
    }

    @Test
    public void fieldInjectTest() {
        IocContainer iocContainer = new IocContainer(AppConfig.class);

        AppService appService = (AppService) iocContainer.getBean("appService");

        System.out.println(appService.getAppRepository());
        assertEquals("John", appService.findUser(1L).getUsername());
    }
}
