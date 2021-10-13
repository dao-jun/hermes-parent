package org.aries.middleware.hermes.plugin.threading;

import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.aries.middleware.hermes.plugin.threading.command.CallableCommand;
import org.aries.middleware.hermes.plugin.threading.command.RunnableCommand;
import io.opentelemetry.context.Context;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author daozhang
 * @apiNote ThreadPoolExecutorInterceptor
 * @since 2020/6/2
 */
public class ThreadPoolExecutorInterceptor implements InstanceMethodsAroundInterceptor {
    private static final ILog log = LogManager.getLogger(ThreadPoolExecutorInterceptor.class);

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {

        if (JudgementUtil.judge(allArguments[0])) {
            Object obj = allArguments[0];
            Context parent = Context.current();

            if (obj instanceof Runnable) {
                RunnableCommand command = new RunnableCommand((Runnable) obj, parent);
                allArguments[0] = command;
            } else if (obj instanceof Callable) {
                CallableCommand<?> command = new CallableCommand<>((Callable<?>) obj, parent);
                allArguments[0] = command;
            }
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret, InvokeContext context) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                                      Throwable t, InvokeContext context) {
    }
}
