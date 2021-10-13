package org.aries.middleware.hermes.plugin.jdbc8;

import cn.hutool.json.JSONUtil;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.aries.middleware.hermes.plugin.jdbc8.info.ConnectionInfo;
import org.aries.middleware.hermes.plugin.jdbc8.info.StatementEnhanceInfo;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 增强connection的createPreparedStatement方法
 *
 * @author daozhang
 * @apiNote PreparedStatementCreateInterceptor
 * @since 2020/5/21
 */
public class PreparedStatementCreateInterceptor implements InstanceMethodsAroundInterceptor {
    private static final ILog logger = LogManager.getLogger(PreparedStatementCreateInterceptor.class);

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, InvokeContext result) throws Throwable {

    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret, InvokeContext context) throws Throwable {
        Object field = objInst.getDynamicField();
        if (Objects.nonNull(field)) {
            StatementEnhanceInfo info = new StatementEnhanceInfo();
            info.setConnectionInfo((ConnectionInfo) field).setSql((String) allArguments[0]).setStatementName("preparedStatement");
            if (Objects.nonNull(ret) && ret instanceof EnhancedInstance) {
                ((EnhancedInstance) ret).setDynamicField(info);
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t, InvokeContext context) {
        logger.error(t, "PreparedStatementCreateInterceptor.handleMethodException,method:{}  args:{}", method.getName(), JSONUtil.toJsonStr(allArguments));
    }
}
