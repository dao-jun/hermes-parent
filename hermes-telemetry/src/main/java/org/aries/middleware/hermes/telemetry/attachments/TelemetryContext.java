package org.aries.middleware.hermes.telemetry.attachments;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Open-telemetry的traceState或者baggage不好用
 * 使用两个Map而不使用TraceLocal是为了兼容异步。InheritableThreadLocal比较麻烦。
 */
public final class TelemetryContext {
    public static final TelemetryContext CONTEXT = new TelemetryContext();
    public static final ContextKey<Map<String, String>> CONTEXT_KEY = ContextKey.named("hermes_context");

    private static final int INIT_SIZE = 2;
    private static final float LOAD_FACTOR = 4;

    //引用计数，当该trace的所有span退出后，再清理
    //主要是处理EntrySpan先于异步的child span结束的情况
    private final Map<String, AtomicInteger> traceSpanCount;
    //traceId -> attachments
    private final Map<String, Map<String, String>> attachments;

    public TelemetryContext() {
        this.traceSpanCount = new ConcurrentHashMap<>(128, 4.0F);
        this.attachments = new ConcurrentHashMap<>(128, 4.0F);
    }


    public void addAttachment(SpanContext spanContext, String key, String value) {
        String traceId = spanContext.getTraceId();

        if (this.traceSpanCount.get(traceId) == null) {
            throw new IllegalStateException();
        }

        Map<String, String> attrs =
                this.attachments.computeIfAbsent(traceId, _k -> new ConcurrentHashMap<>(INIT_SIZE, LOAD_FACTOR));

        attrs.put(key, value);
    }

    public void addAttachments(SpanContext spanContext, Map<String, String> attachments) {
        String traceId = spanContext.getTraceId();

        if (this.traceSpanCount.get(traceId) == null) {
            throw new IllegalStateException();
        }

        Map<String, String> attrs =
                this.attachments.computeIfAbsent(traceId, _k -> new ConcurrentHashMap<>(INIT_SIZE, LOAD_FACTOR));

        attrs.putAll(attachments);
    }

    public String removeAttachment(SpanContext spanContext, String key) {
        String traceId = spanContext.getTraceId();

        Map<String, String> attrs = this.attachments.get(traceId);
        if (null != attrs) {
            return attrs.remove(key);
        }

        return null;
    }


    public String getAttachment(SpanContext spanContext, String key) {
        String traceId = spanContext.getTraceId();

        Map<String, String> attrs = this.attachments.get(traceId);
        if (null != attrs) {
            return attrs.get(key);
        }

        return null;
    }


    public Map<String, String> getAttachments(SpanContext spanContext) {
        String traceId = spanContext.getTraceId();
        Map<String, String> attrs = this.attachments.get(traceId);

        if (attrs == null) {
            return Map.of();
        } else {
            ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                builder.put(entry.getKey(), entry.getValue());
            }

            return builder.build();
        }
    }

    public void clearAttachments(Span span) {
        SpanContext spanContext = span.getSpanContext();
        String traceId = spanContext.getTraceId();

        Map<String, String> attrs = this.attachments.get(traceId);
        if (null != attrs) {
            attrs.clear();
        }
    }

    /**
     * span开始时，初始化attachments
     * span结束时，自动清理attachments
     */
    public static class ContextSpanProcessor implements SpanProcessor {

        /**
         * 当entry span进入时，初始化
         *
         * @param parentContext
         * @param span
         */
        @Override
        public void onStart(Context parentContext, ReadWriteSpan span) {
            SpanContext spanContext = span.getSpanContext();
            String traceId = spanContext.getTraceId();

            AtomicInteger count =
                    CONTEXT.traceSpanCount.computeIfAbsent(traceId, k -> new AtomicInteger(0));

            Map<String, String> attachments = parentContext.get(CONTEXT_KEY);
            if (count.getAndIncrement() == 0 && null != attachments) {
                CONTEXT.addAttachments(spanContext, parentContext.get(CONTEXT_KEY));
            }
        }

        @Override
        public boolean isStartRequired() {
            return true;
        }

        @Override
        public void onEnd(ReadableSpan span) {
            SpanContext spanContext = span.getSpanContext();
            String traceId = spanContext.getTraceId();

            AtomicInteger entrySpanId = TelemetryContext.CONTEXT.traceSpanCount.get(traceId);
            if (entrySpanId.decrementAndGet() == 0) {
                TelemetryContext.CONTEXT.traceSpanCount.remove(traceId);
                TelemetryContext.CONTEXT.attachments.remove(traceId);
            }
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }
    }
}
