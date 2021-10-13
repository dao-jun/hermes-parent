package org.aries.middleware.hermes.plugin.lettuce5;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceConstructorInterceptor;

public class RedisClusterClientConstructorInterceptor implements InstanceConstructorInterceptor {

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        @SuppressWarnings("unchecked") Iterable<RedisURI> redisURIs = (Iterable<RedisURI>) allArguments[1];
        RedisClusterClient redisClusterClient = (RedisClusterClient) objInst;
        StringBuilder peer = new StringBuilder();
        for (RedisURI redisURI : redisURIs) {
            peer.append(redisURI.getHost()).append(":").append(redisURI.getPort()).append(";");
        }
        EnhancedInstance optionsInst = (EnhancedInstance) redisClusterClient.getOptions();
        optionsInst.setDynamicField(PeerFormat.shorten(peer.toString()));
    }
}
