package org.aries.middleware.hermes.plugin.elasticsearch;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContextUtil;
import org.aries.middleware.hermes.common.pojo.Tuple2;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import java.lang.reflect.Method;
import java.util.List;

public class RestClientMethodInterceptor implements InstanceMethodsAroundInterceptor {

    private static final String INSTRUMENTATION_NAME = "ElasticSearch";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             InvokeContext context) throws Throwable {
        initNodes(objInst);

        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        Tracer tracer = telemetry.getTracer(INSTRUMENTATION_NAME);

        Context current = Context.current();
        Span parent = Span.fromContext(current);

        if (parent.getSpanContext().isSampled()) {

            Tuple2<String, String> tuple2 = this.buildOperationName(allArguments);
            Span span = tracer.spanBuilder(tuple2.f1)
                    .setParent(current)
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute(SemanticAttributes.HTTP_METHOD, tuple2.f2)
                    .setAttribute(SemanticAttributes.HTTP_HOST, (String) objInst.getDynamicField())
                    .startSpan();

            InvokeContextUtil.makeCurrent(span, context);
        }
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


    void initNodes(EnhancedInstance objInst) {
        if (null == objInst.getDynamicField()) {
            RestClient client = (RestClient) objInst;
            List<Node> nodes = client.getNodes();
            StringBuilder builder = new StringBuilder();
            for (Node node : nodes) {
                builder.append(node.getHost().toURI()).append(",");
            }
            objInst.setDynamicField(builder.toString());
        }
    }


    Tuple2<String, String> buildOperationName(Object[] allArguments) {
        Object arg = allArguments[0];
        if (arg instanceof Request) {
            Request request = (Request) arg;
            String endpoint = request.getEndpoint();
            return Tuple2.of("ElasticSearch:/" + endpoint, request.getMethod());
        }

        return Tuple2.of("ElasticSearch://UnknownOperation", "Unknown");
    }
}

