package org.aries.middleware.hermes.telemetry;

import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.NettyChannelBuilder;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class HermesSpanExporter implements SpanExporter {
    private final SpanExporter exporter;

    HermesSpanExporter(String host, int port, String auth) {
        this.exporter = JaegerGrpcSpanExporter.builder()
                .setTimeout(10, TimeUnit.SECONDS)
                .setChannel(this.managedChannel(host, port, auth))
                .build();
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        return this.exporter.export(spans);
    }

    @Override
    public CompletableResultCode flush() {
        return this.exporter.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return this.exporter.shutdown();
    }

    @Override
    public void close() {
        this.exporter.close();
    }


    private ManagedChannel managedChannel(String host, int port, String token) {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());

        return NettyChannelBuilder
                .forAddress(host, port)
                .intercept(
                        new ClientInterceptor() {
                            @Override
                            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                                    MethodDescriptor<ReqT, RespT> descriptor, CallOptions options, Channel channel) {
                                return new ForwardingClientCall
                                        .SimpleForwardingClientCall<>(channel.newCall(descriptor, options)) {
                                    @Override
                                    public void start(Listener<RespT> responseListener, final Metadata headers) {
                                        Metadata.Key<String> headerKey = Metadata.Key.of("Authentication", Metadata.ASCII_STRING_MARSHALLER);
                                        headers.put(headerKey, token);
                                        super.start(responseListener, headers);
                                    }
                                };
                            }
                        }
                )
                .nameResolverFactory(new DnsNameResolverProvider())
                .usePlaintext()
                .build();
    }

    public static SpanExporter of() {
        String host = "tracing-analysis-dc-us-east-1.aliyuncs.com";
        int port = 1883;
        String auth = "1j8swcnon5p@06aafcaa5ecd1ef_1j8swcnon5p@53df7ad2afe8301";
        return new HermesSpanExporter(host, port, auth);
    }
}
