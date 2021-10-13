package org.aries.middleware.hermes.plugin.lettuce5;

import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceConstructorInterceptor;

/**
 * ClientOptions is the link between RedisChannelWriter and AbstractRedisClient. to enhance ClientOptions for bring
 * peer(the cluster configuration information) in AbstractRedisClient to RedisChannelWriter.
 */
public class ClientOptionsConstructorInterceptor implements InstanceConstructorInterceptor {

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
    }
}
