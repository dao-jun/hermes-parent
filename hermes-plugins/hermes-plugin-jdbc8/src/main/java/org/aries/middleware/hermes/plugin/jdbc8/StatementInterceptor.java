package org.aries.middleware.hermes.plugin.jdbc8;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContextUtil;
import org.aries.middleware.hermes.plugin.jdbc8.info.ConnectionInfo;
import org.aries.middleware.hermes.plugin.jdbc8.info.StatementEnhanceInfo;

import java.lang.reflect.Method;
import java.util.Objects;

import static org.aries.middleware.hermes.plugin.jdbc8.PrepareStatementInterceptor.INSTRUMENTATION_NAME;
import static org.aries.middleware.hermes.plugin.jdbc8.util.SqlUtil.isSql;

/**
 * 增强Statement的execute方法
 *
 * @author daozhang
 * @apiNote StatementInterceptor
 * @since 2020/5/21
 */
public class StatementInterceptor implements InstanceMethodsAroundInterceptor {
    private static final ILog logger = LogManager.getLogger(StatementInterceptor.class);

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {
        Object field = objInst.getDynamicField();
        if (Objects.isNull(field))
            return;

        StatementEnhanceInfo info = (StatementEnhanceInfo) field;
        ConnectionInfo connInfo = info.getConnectionInfo();
        String sql = allArguments.length > 0 ? (String) allArguments[0] : "";

        if (!isSql(sql)) {
            return;
        }

        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);

        Span span = tracer.spanBuilder("JDBC8://Statement/" + method.getName())
                .setParent(Context.current())
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute(SemanticAttributes.DB_STATEMENT, info.getSql())
                .setAttribute(SemanticAttributes.DB_NAME, connInfo.getDatabaseName())
                .setAttribute(SemanticAttributes.DB_CONNECTION_STRING, connInfo.getDatabasePeer())
                .setAttribute(SemanticAttributes.DB_SYSTEM, connInfo.getDBType())
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
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                                      Throwable t, InvokeContext context) {
        InvokeContextUtil.recordExcept(t, context);
    }
}
