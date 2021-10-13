package org.aries.middleware.hermes.plugin.lettuce5;

import io.lettuce.core.AbstractRedisClient;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;

import java.lang.reflect.Method;

public class AbstractRedisClientInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {
        EnhancedInstance clientOptions = (EnhancedInstance) allArguments[0];
        if (clientOptions == null) {
            return;
        }
        AbstractRedisClient client = (AbstractRedisClient) objInst;
        if (client.getOptions() == null || ((EnhancedInstance) client.getOptions()).getDynamicField() == null) {
            return;
        }
        clientOptions.setDynamicField(((EnhancedInstance) client.getOptions()).getDynamicField());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret, InvokeContext context) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t, InvokeContext context) {
    }
}
