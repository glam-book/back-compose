package com.tlback.core.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * The SpringBeanJobFactory provides support for injecting the scheduler
 * context,
 * job data map, and trigger data entries as properties into the
 * job bean while creating an instance.
 *
 * However, it lacks support for injecting bean references from the application
 * context.
 * 
 * @author vyacheslav vorobev
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}
