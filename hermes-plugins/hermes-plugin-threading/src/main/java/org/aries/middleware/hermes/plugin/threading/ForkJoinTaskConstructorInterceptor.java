package org.aries.middleware.hermes.plugin.threading;

import io.opentelemetry.context.Context;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceConstructorInterceptor;

/**
 * @author daozhang
 * @apiNote ForkJoinTaskConstructorInterceptor
 * @since 2020/6/4
 */
public class ForkJoinTaskConstructorInterceptor implements InstanceConstructorInterceptor {
    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        objInst.setDynamicField(Context.current());
    }
}
