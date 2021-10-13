package org.aries.middleware.hermes.plugin.threading.command;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import lombok.AllArgsConstructor;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;

import java.util.concurrent.Callable;

import static java.lang.Thread.currentThread;

@AllArgsConstructor
public class CallableCommand<T> implements Callable<T> {
    private static final ILog log = LogManager.getLogger(CallableCommand.class);
    private static final String INSTRUMENTATION_NAME = "Callable";

    private final Callable<T> callable;
    private final Context context;

    @Override
    public T call() throws Exception {
        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder("Callable://" + this.callable.getClass().getSimpleName() + "/call")
                .setParent(context)
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SemanticAttributes.THREAD_NAME, currentThread().getName())
                .startSpan()
                .setStatus(StatusCode.OK);

        try (Scope scope = span.makeCurrent()) {
            return this.callable.call();
        } catch (Exception e) {
            span.recordException(e)
                    .setStatus(StatusCode.ERROR);
            log.error(e, "Callable Command {} Executing Failed", callable.getClass().getName());
            throw e;
        } finally {
            span.end();
        }
    }
}
