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

import static org.aries.middleware.hermes.plugin.jdbc8.util.SqlUtil.isSql;


/**
 * 增强PrepareStatement的 executeXX方法
 *
 * @author daozhang
 * @apiNote PrepareStatementInterceptor
 * @since 2020/5/21
 */
public class PrepareStatementInterceptor implements InstanceMethodsAroundInterceptor {
    private static final ILog logger = LogManager.getLogger(PrepareStatementInterceptor.class);
    public static final String INSTRUMENTATION_NAME = "JDBC8";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, InvokeContext context) throws Throwable {
        Object field = objInst.getDynamicField();
        if (Objects.isNull(field))
            return;

        StatementEnhanceInfo info = (StatementEnhanceInfo) field;
        ConnectionInfo connInfo = info.getConnectionInfo();
        String sql = info.getSql();
        if (!isSql(sql)) {
            return;
        }

        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);

        Span span = tracer.spanBuilder("JDBC8://PrepareStatement/" + method.getName())
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
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret, InvokeContext context) throws Throwable {
        if (Objects.isNull(objInst.getDynamicField())) {
            return ret;
        }

        StatementEnhanceInfo info = (StatementEnhanceInfo) objInst.getDynamicField();
        if (!isSql(info.getSql())) {
            return ret;
        }

        InvokeContextUtil.releaseScope(context);
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t, InvokeContext context) {
        InvokeContextUtil.recordExcept(t, context);
    }
}
