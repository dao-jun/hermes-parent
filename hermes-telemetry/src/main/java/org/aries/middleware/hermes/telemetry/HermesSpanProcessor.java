package org.aries.middleware.hermes.telemetry;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.extension.trace.export.DisruptorAsyncSpanProcessor;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.aries.middleware.hermes.telemetry.attachments.TelemetryContext;

import java.util.Objects;

public final class HermesSpanProcessor implements SpanProcessor {

    private final SpanProcessor processor;

    HermesSpanProcessor(SpanExporter exporter) {
        SpanProcessor simple = BatchSpanProcessor
                .builder(exporter)
                .build();

        SpanProcessor contextProcessor = new TelemetryContext.ContextSpanProcessor();

        this.processor = DisruptorAsyncSpanProcessor
                .builder(SpanProcessor.composite(simple, contextProcessor))
                .build();
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        this.processor.onStart(parentContext, span);
    }

    @Override
    public boolean isStartRequired() {
        return this.processor.isStartRequired();
    }

    @Override
    public void onEnd(ReadableSpan span) {
        this.processor.onEnd(span);
    }

    @Override
    public boolean isEndRequired() {
        return this.processor.isEndRequired();
    }

    @Override
    public CompletableResultCode shutdown() {
        return this.processor.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return this.processor.forceFlush();
    }

    @Override
    public void close() {
        this.processor.close();
    }


    public static SpanProcessor of(SpanExporter exporter) {
        return new HermesSpanProcessor(Objects.requireNonNull(exporter));
    }
}
