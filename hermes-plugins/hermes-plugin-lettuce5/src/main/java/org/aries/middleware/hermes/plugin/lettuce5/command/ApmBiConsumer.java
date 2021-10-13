package org.aries.middleware.hermes.plugin.lettuce5.command;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.function.BiConsumer;

import static org.aries.middleware.hermes.plugin.lettuce5.AsyncCommandMethodInterceptor.INSTRUMENTATION_NAME;

public class ApmBiConsumer<T, U> implements BiConsumer<T, U> {

    private final BiConsumer<T, U> biConsumer;
    private final Context snapshot;
    private final String operationName;

    public ApmBiConsumer(BiConsumer<T, U> biConsumer, Context snapshot, String operationName) {
        this.biConsumer = biConsumer;
        this.snapshot = snapshot;
        this.operationName = operationName;
    }

    @Override
    public void accept(T t, U u) {
        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder(operationName + "/accept")
                .setSpanKind(SpanKind.CLIENT)
                .setParent(snapshot)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            biConsumer.accept(t, u);
        } catch (Throwable th) {
            span.recordException(th);
        } finally {
            span.end();
        }
    }
}
