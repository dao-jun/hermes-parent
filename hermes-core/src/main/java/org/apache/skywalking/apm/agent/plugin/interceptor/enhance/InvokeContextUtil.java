package org.apache.skywalking.apm.agent.plugin.interceptor.enhance;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aries.middleware.hermes.telemetry.attachments.TelemetryContext;

import java.util.List;
import java.util.Map;

public class InvokeContextUtil {
//    /**
//     * 添加attachment到当前链路上下文中，下级所有Span可见
//     * 应当在 InvokeContextUtil#makeCurrent方法后调用
//     *
//     * @param attachments
//     * @param context
//     */
//    public static void put(Map<String, String> attachments, InvokeContext context) {
//
//        if (null != context.span() && null != context.scopes()) {
//            Context current = Context.current();
//            if (current == Context.root())
//                throw new IllegalStateException();
//
//            Baggage baggage = Baggage.fromContext(current);
//
//            BaggageBuilder builder = baggage.toBuilder();
//            for (Map.Entry<String, String> entry : attachments.entrySet())
//                builder.put(entry.getKey(), entry.getKey());
//
//            context.scope(builder.build().makeCurrent());
//        }
//
//        throw new IllegalStateException();
//    }
//
//    /**
//     * 从当前链路上下文中读取attachment信息
//     * 应当在 InvokeContextUtil#makeCurrent方法后调用
//     *
//     * @param key
//     * @param context
//     * @return
//     */
//    public static String get(String key, InvokeContext context) {
//        Context current = Context.current();
//        if (current == Context.root())
//            throw new IllegalStateException();
//
//        if (null != context.scopes() && null != context.span()) {
//            Baggage baggage = Baggage.current();
//            return baggage.getEntryValue(key);
//        }
//
//        throw new IllegalStateException();
//    }
//
//    /**
//     * 从当前链路上下文中移除相关attachments
//     * 应当在 InvokeContextUtil#makeCurrent方法后调用
//     *
//     * @param keys
//     * @param context
//     */
//    public static void remove(List<String> keys, InvokeContext context) {
//        Context current = Context.current();
//        if (current == Context.root())
//            throw new IllegalStateException();
//
//        if (null != context.scopes() && null != context.span()) {
//            Baggage baggage = Baggage.current();
//            BaggageBuilder builder = baggage.toBuilder();
//            for (String key : keys) {
//                builder.remove(key);
//            }
//
//            context.scope(builder.build().makeCurrent());
//        }
//
//        throw new IllegalStateException();
//    }

//    public static void makeCurrent(Span span, InvokeContext context) {
//        Baggage baggage = Baggage.current();
//
//        context.span(span.setStatus(StatusCode.OK))
//                .scope(span.makeCurrent());
//               .scope(baggage.makeCurrent());
//    }

    /**
     * 添加attachment到当前链路上下文中，下级所有Span可见
     * 应当在 InvokeContextUtil#makeCurrent方法后调用
     *
     * @param attachments
     * @param context
     */
    public static void put(Map<String, String> attachments, InvokeContext context) {

        if (null != context.span() && null != context.scope()) {
            Context current = Context.current();
            if (current == Context.root())
                throw new IllegalStateException();

            Span span = (Span) context.span();
            TelemetryContext.CONTEXT.addAttachments(span.getSpanContext(), attachments);
        }

        throw new IllegalStateException();
    }

    /**
     * 从当前链路上下文中读取attachment信息
     * 应当在 InvokeContextUtil#makeCurrent方法后调用
     *
     * @param key
     * @param context
     * @return
     */
    public static String get(String key, InvokeContext context) {

        if (null != context.span() && null != context.scope()) {
            Context current = Context.current();
            if (current == Context.root())
                throw new IllegalStateException();

            Span span = (Span) context.span();
            TraceState state = span.getSpanContext().getTraceState();
            return state.get(key);
        }

        throw new IllegalStateException();
    }

    /**
     * 从当前链路上下文中移除相关attachments
     * 应当在 InvokeContextUtil#makeCurrent方法后调用
     *
     * @param keys
     * @param context
     */
    public static void remove(List<String> keys, InvokeContext context) {

        if (null != context.span() && null != context.scope()) {
            Context current = Context.current();
            if (current == Context.root())
                throw new IllegalStateException();

            Span span = (Span) context.span();
            SpanContext spanContext = span.getSpanContext();
            for (String key : keys)
                TelemetryContext.CONTEXT.removeAttachment(spanContext, key);
        }

        throw new IllegalStateException();
    }

//    public static void makeCurrent(Span span, InvokeContext context) {
//        Baggage baggage = Baggage.current();
//
//        context.span(span.setStatus(StatusCode.OK))
//                .scope(span.makeCurrent());
//               .scope(baggage.makeCurrent());
//    }

    public static void makeCurrent(Span span, InvokeContext context) {

        context.span(span.setStatus(StatusCode.OK))
                .scope(span.makeCurrent());
    }


    public static void context(Object context, InvokeContext invokeContext) {
        invokeContext.context(context);
    }

    public static <T> T context(InvokeContext context) {
        Object object = context.context();
        return null == object ? null : (T) object;
    }

//    public static void releaseScope(InvokeContext context) {
//        if (null != context.span() && null != context.scopes()) {
//            Span span = (Span) context.span();
//            Deque<Object> scopes = context.scopes();
//            span.end();
//
//            Scope scope;
//            while ((scope = (Scope) scopes.pollLast()) != null) {
//                scope.close();
//            }
//        }
//    }

    public static Span getSpan(InvokeContext context) {
        Object span = context.span();
        return span == null ? Span.getInvalid() : (Span) span;
    }

    public static void releaseScope(InvokeContext context) {
        if (null != context.span() && null != context.scope()) {
            Span span = (Span) context.span();
            Scope scope = (Scope) context.scope();
            span.end();
            scope.close();
        }
    }

    public static void recordExcept(Throwable t, InvokeContext context) {
        if (null != context.span()) {
            Span span = (Span) context.span();
            span.setStatus(StatusCode.ERROR)
                    .recordException(t);
        }
    }
}
