package org.aries.middleware.hermes.plugin.threading;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContextUtil;

import java.lang.reflect.Method;
import java.util.Objects;

import static java.lang.Thread.currentThread;

/**
 * @author daozhang
 * @apiNote ForkJoinTaskMethodInterceptor
 * @since 2020/6/4
 */
public class ForkJoinTaskMethodInterceptor implements InstanceMethodsAroundInterceptor {

    private static final String OPERATION_NAME = "threading://java.util.concurrent.ForkJoinTask/doExec";
    private static final String INSTRUMENTATION_NAME = "ForkJoinTask";
    private static final ILog log = LogManager.getLogger(ForkJoinTaskMethodInterceptor.class);

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {
        log.info("ready to intercept fork join task. obj:{}", objInst);

        Object o = objInst.getDynamicField();
        if (Objects.nonNull(o) && o instanceof Context) {
            Context parent = (Context) o;

            OpenTelemetry telemetry = GlobalOpenTelemetry.get();
            Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
            Span span = tracer.spanBuilder(OPERATION_NAME)
                    .setParent(parent)
                    .setSpanKind(SpanKind.INTERNAL)
                    .setAttribute(SemanticAttributes.THREAD_NAME, currentThread().getName())
                    .startSpan();

            InvokeContextUtil.makeCurrent(span, context);
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret, InvokeContext context) throws Throwable {

        InvokeContextUtil.releaseScope(context);
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                                      Throwable t, InvokeContext context) {

        InvokeContextUtil.recordExcept(t, context);
    }
}
