package com.starexchangealliance.shared.utils.tests.api.framework.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public abstract class AbstractOperation implements Operation {

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    protected <T> T newInstance(Class<T> cls) {
        try {
            T newInstance = cls.newInstance();
            beanFactory.autowireBean(newInstance);
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
