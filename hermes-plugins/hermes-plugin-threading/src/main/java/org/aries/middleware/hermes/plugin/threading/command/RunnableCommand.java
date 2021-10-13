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

import static java.lang.Thread.currentThread;

@AllArgsConstructor
public class RunnableCommand implements Runnable {
    private static final ILog log = LogManager.getLogger(RunnableCommand.class);
    private static final String INSTRUMENTATION_NAME = "Runnable";

    private final Runnable runnable;
    private final Context context;

    @Override
    public void run() {
        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder("Runnable://" + this.runnable.getClass().getSimpleName() + "/run")
                .setParent(context)
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SemanticAttributes.THREAD_NAME, currentThread().getName())
                .startSpan()
                .setStatus(StatusCode.OK);

        try (Scope scope = span.makeCurrent()) {
            this.runnable.run();
        } catch (Exception e) {
            span.recordException(e)
                    .setStatus(StatusCode.ERROR);
            log.error(e, "Callable Command {} Executing Failed", runnable.getClass().getName());
            throw e;
        } finally {
            span.end();
        }
    }
}
