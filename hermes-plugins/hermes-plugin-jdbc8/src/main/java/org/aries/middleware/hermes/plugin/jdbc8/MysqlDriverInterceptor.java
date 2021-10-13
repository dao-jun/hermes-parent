package org.aries.middleware.hermes.plugin.jdbc8;

import cn.hutool.json.JSONUtil;
import com.mysql.cj.conf.HostInfo;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;
import org.aries.middleware.hermes.plugin.jdbc8.info.ConnectionInfo;
import org.aries.middleware.hermes.plugin.jdbc8.util.URLParser;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author daozhang
 * @apiNote MysqlDriverInterceptor
 * @since 2020/5/21
 */
public class MysqlDriverInterceptor implements StaticMethodsAroundInterceptor {
    private static final ILog logger = LogManager.getLogger(MysqlDriverInterceptor.class);

    @Override
    public void beforeMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, InvokeContext context) {

    }

    @Override
    public Object afterMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Object ret, InvokeContext context) {
        if (Objects.nonNull(ret) && ret instanceof EnhancedInstance) {
            EnhancedInstance instance = (EnhancedInstance) ret;
            if (allArguments.length == 5) {
                ConnectionInfo info = URLParser.parser((String) allArguments[4]);
                instance.setDynamicField(info);
                logger.info("set dynamic field for jdbc5, url:{} info:{}", allArguments[4].toString(), info);
            } else if (allArguments.length == 1) {
                HostInfo hostInfo = (HostInfo) allArguments[0];
                ConnectionInfo info = URLParser.parser(hostInfo.getDatabaseUrl());
                instance.setDynamicField(info);
                logger.info("set dynamic field for jdbc8, url:{}  info:{}", hostInfo.getDatabaseUrl(), info);
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Throwable t, InvokeContext context) {
        logger.error(t, "MysqlDriverInterceptor.handleMethodException,method:{}  args:{}", method.getName(), JSONUtil.toJsonStr(allArguments));
    }
}
