package com.wscrg;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

public class IocContainerTest {

    @Test
    public void registerBeanTest() {
        IocContainer iocContainer = new IocContainer(AppConfig.class);

        Set<String> beans = iocContainer.getBeans();
        assertEquals(3, beans.size());
    }
}
