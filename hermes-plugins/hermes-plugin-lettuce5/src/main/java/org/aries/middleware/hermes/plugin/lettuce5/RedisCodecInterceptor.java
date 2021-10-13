package org.aries.middleware.hermes.plugin.lettuce5;

import cn.hutool.json.JSONUtil;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author daozhang
 * @apiNote RedisCodecInterceptor
 * @since 2020/7/30
 */
public class RedisCodecInterceptor implements InstanceMethodsAroundInterceptor {
    private static final ILog log = LogManager.getLogger(RedisCodecInterceptor.class);

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, InvokeContext context) throws Throwable {
        if (Objects.nonNull(allArguments) && allArguments.length > 0 && Objects.nonNull(allArguments[0])
                && allArguments[0] instanceof byte[]) {
            byte[] key = (byte[]) allArguments[0];
            log.info("key:{}", JSONUtil.toJsonStr(key));
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret, InvokeContext context) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t, InvokeContext context) {

    }
}
