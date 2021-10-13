package org.aries.middleware.hermes.plugin.lettuce5;

import io.lettuce.core.protocol.RedisCommand;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.*;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.aries.middleware.hermes.plugin.lettuce5.AsyncCommandMethodInterceptor.INSTRUMENTATION_NAME;

public class RedisChannelWriterInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {
        String peer = (String) objInst.getDynamicField();

        StringBuilder dbStatement = new StringBuilder();
        String operationName = "Lettuce/";

        if (allArguments[0] instanceof RedisCommand) {
            RedisCommand redisCommand = (RedisCommand) allArguments[0];
            String command = redisCommand.getType().name();
            operationName = operationName + command;
            dbStatement.append(command);
        } else if (allArguments[0] instanceof Collection) {
            @SuppressWarnings("unchecked") Collection<RedisCommand> redisCommands = (Collection<RedisCommand>) allArguments[0];
            operationName = operationName + "BATCH_WRITE";
            for (RedisCommand redisCommand : redisCommands) {
                dbStatement.append(redisCommand.getType().name()).append(";");
            }
        }

        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer
                .spanBuilder(operationName)
                .setSpanKind(SpanKind.CLIENT)
                .setParent(Context.current())
                .setAttribute(SemanticAttributes.NET_HOST_IP, peer)
                .setAttribute(SemanticAttributes.DB_SYSTEM, "Redis")
                .setAttribute(SemanticAttributes.DB_STATEMENT, dbStatement.toString())
                .startSpan();

        InvokeContextUtil.makeCurrent(span, context);
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

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        EnhancedInstance optionsInst = (EnhancedInstance) allArguments[0];
        objInst.setDynamicField(optionsInst.getDynamicField());
    }
}
