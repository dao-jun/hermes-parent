package org.aries.middleware.hermes.telemetry.attachments;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.opentelemetry.api.internal.Utils.checkArgument;

/**
 * 处理attachments 跨进程
 */
public class AttachmentsPropagator implements TextMapPropagator {
    public static final TextMapPropagator INSTANCE = new AttachmentsPropagator();

    private final static String TRACE_ATTRS = "traceattachments";
    private final static List<String> FIELDS = List.of(TRACE_ATTRS);

    private static final char TRACESTATE_KEY_VALUE_DELIMITER = '=';
    private static final char TRACESTATE_ENTRY_DELIMITER = ',';
    private static final int TRACESTATE_MAX_MEMBERS = 32;
    private static final Pattern TRACESTATE_ENTRY_DELIMITER_SPLIT_PATTERN =
            Pattern.compile("[ \t]*" + TRACESTATE_ENTRY_DELIMITER + "[ \t]*");

    @Override
    public Collection<String> fields() {
        return FIELDS;
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
        if (context == null || setter == null) {
            return;
        }

        SpanContext spanContext = Span.fromContext(context).getSpanContext();
        Map<String, String> attachments = TelemetryContext.CONTEXT.getAttachments(spanContext);
        if (MapUtil.isEmpty(attachments)) {
            return;
        }
        StringBuilder headerContent = new StringBuilder();
        attachments.forEach(
                (key, value) -> {
                    if (headerContent.length() != 0) {
                        headerContent.append(TRACESTATE_ENTRY_DELIMITER);
                    }
                    headerContent.append(key).append(TRACESTATE_KEY_VALUE_DELIMITER).append(value);
                });

        setter.set(carrier, TRACE_ATTRS, headerContent.toString());
    }

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
        if (context == null) {
            return Context.root();
        }
        if (getter == null) {
            return context;
        }
        String attrHeader = getter.get(carrier, TRACE_ATTRS);
        if (StrUtil.isEmpty(attrHeader)) {
            return context;
        }

        Map<String, String> attachments = new HashMap<>();

        String[] listMembers = TRACESTATE_ENTRY_DELIMITER_SPLIT_PATTERN.split(attrHeader);
        checkArgument(
                listMembers.length <= TRACESTATE_MAX_MEMBERS, "TraceState has too many elements.");
        // Iterate in reverse order because when call builder set the elements is added in the
        // front of the list.
        for (int i = listMembers.length - 1; i >= 0; i--) {
            String listMember = listMembers[i];
            int index = listMember.indexOf(TRACESTATE_KEY_VALUE_DELIMITER);
            checkArgument(index != -1, "Invalid TraceState list-member format.");
            attachments.put(listMember.substring(0, index), listMember.substring(index + 1));
        }

        return context.with(TelemetryContext.CONTEXT_KEY, attachments);
    }
}
