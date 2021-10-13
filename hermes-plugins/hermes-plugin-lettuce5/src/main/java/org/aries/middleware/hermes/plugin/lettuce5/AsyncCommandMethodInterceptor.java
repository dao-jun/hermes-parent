package org.aries.middleware.hermes.plugin.lettuce5;

import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContextUtil;
import org.aries.middleware.hermes.plugin.lettuce5.command.ApmBiConsumer;
import org.aries.middleware.hermes.plugin.lettuce5.command.ApmConsumer;
import io.lettuce.core.protocol.AsyncCommand;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AsyncCommandMethodInterceptor implements InstanceMethodsAroundInterceptor {

    public static final String INSTRUMENTATION_NAME = "Lettuce5";

    @Override
    @SuppressWarnings("unchecked")
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {
        AsyncCommand asyncCommand = (AsyncCommand) objInst;
        String operationName = "Lettuce://" + asyncCommand.getType().name();

        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder(operationName + "/onComplete")
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

        InvokeContextUtil.makeCurrent(span, context);
        Context parent = Context.current();

        if (allArguments[0] instanceof Consumer) {
            allArguments[0] = new ApmConsumer((Consumer) allArguments[0], parent, operationName);
        } else {
            allArguments[0] = new ApmBiConsumer((BiConsumer) allArguments[0], parent, operationName);
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret, InvokeContext context) throws Throwable {
        InvokeContextUtil.releaseScope(context);
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t, InvokeContext context) {
        InvokeContextUtil.recordExcept(t, context);
    }
}
