package org.aries.middleware.hermes.plugin.jdbc8;

import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.aries.middleware.hermes.plugin.jdbc8.info.StatementEnhanceInfo;

import java.lang.reflect.Method;

/**
 * @author daozhang
 * @apiNote PreparedStatementSetterInterceptor
 * @since 2020/8/13
 */
public class PreparedStatementSetterInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, InvokeContext result) throws Throwable {
        Object o = objInst.getDynamicField();
        StatementEnhanceInfo info = (StatementEnhanceInfo) o;
        int index = (int) allArguments[0];
        Object value = allArguments[1];
//        info.addParam(index, value);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret, InvokeContext context) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t, InvokeContext context) {

    }
}
