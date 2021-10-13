package org.aries.middleware.hermes.plugin.lettuce5.command;


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.function.Consumer;

import static org.aries.middleware.hermes.plugin.lettuce5.AsyncCommandMethodInterceptor.INSTRUMENTATION_NAME;

public class ApmConsumer<T> implements Consumer<T> {

    private final Consumer<T> consumer;
    private final Context snapshot;
    private final String operationName;

    public ApmConsumer(Consumer<T> consumer, Context snapshot, String operationName) {
        this.consumer = consumer;
        this.snapshot = snapshot;
        this.operationName = operationName;
    }

    @Override
    public void accept(T t) {
        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder(operationName + "/accept")
                .setSpanKind(SpanKind.CLIENT)
                .setParent(snapshot)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            consumer.accept(t);
        } catch (Throwable th) {
            span.recordException(th);
        } finally {
            span.end();
        }
    }
}
