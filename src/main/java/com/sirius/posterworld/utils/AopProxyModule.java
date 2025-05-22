package com.sirius.posterworld.utils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.aop.Advisor;

public class AopProxyModule extends SimpleModule {

    public AopProxyModule() {
        super("AopProxyModule", new Version(1, 0, 0, null, null, null));
        setMixInAnnotation(org.springframework.aop.framework.Advised.class, AdvisedMixIn.class);
    }

    private interface AdvisedMixIn {
        @com.fasterxml.jackson.annotation.JsonIgnore
        Advisor[] getAdvisors();

        @com.fasterxml.jackson.annotation.JsonIgnore
        org.springframework.aop.TargetSource getTargetSource();

        @com.fasterxml.jackson.annotation.JsonIgnore
        boolean isProxyTargetClass();

        @com.fasterxml.jackson.annotation.JsonIgnore
        Object getWrappedObject();
    }
}
