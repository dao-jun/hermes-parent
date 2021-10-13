package org.apache.skywalking.apm.agent.service;

import cn.hutool.core.util.RandomUtil;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.apache.skywalking.apm.agent.boot.BootService;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.aries.middleware.hermes.telemetry.HermesIdGenerator;
import org.aries.middleware.hermes.telemetry.HermesSpanExporter;
import org.aries.middleware.hermes.telemetry.HermesSpanProcessor;
import org.aries.middleware.hermes.telemetry.attachments.AttachmentsPropagator;

import static org.apache.skywalking.apm.agent.config.Config.Application.ENV;
import static org.apache.skywalking.apm.agent.config.Config.Application.NAME;


public class TelemetryBootService implements BootService {
    private static final ILog log = LogManager.getLogger(TelemetryBootService.class);

    private SdkTracerProvider provider;

    @Override
    public void prepare() throws Throwable {
        log.info("Open Telemetry Preparing...");
    }

    @Override
    public void boot() throws Throwable {
        log.info("Open Telemetry Boot Finished");
    }

    @Override
    public void onComplete() throws Throwable {
        TextMapPropagator attachmentsPropagator = AttachmentsPropagator.INSTANCE;
        TextMapPropagator tracePropagator = W3CTraceContextPropagator.getInstance();

        TextMapPropagator propagator = TextMapPropagator.composite(tracePropagator, attachmentsPropagator);
        ContextPropagators propagators = ContextPropagators.create(propagator);

        SpanExporter exporter = HermesSpanExporter.of();
        SpanProcessor processor = HermesSpanProcessor.of(exporter);

        Attributes attributes = Attributes.of(
                AttributeKey.stringKey("service.name"), NAME,
                AttributeKey.stringKey("service.env"), ENV
        );

        this.provider = SdkTracerProvider
                .builder()
                .setClock(Clock.getDefault())
                .setIdGenerator(HermesIdGenerator.of(RandomUtil.randomInt()))
                .setSpanLimits(SpanLimits.getDefault())
                .setSampler(Sampler.alwaysOn())
                .setResource(Resource.create(attributes))
                .addSpanProcessor(processor)
                .build();

        OpenTelemetrySdk.builder()
                .setPropagators(propagators)
                .setTracerProvider(this.provider)
                .buildAndRegisterGlobal();

        log.info("Open Telemetry Completing...");
    }

    @Override
    public void shutdown() throws Throwable {
        if (null != this.provider)
            this.provider.shutdown();

        log.info("Open Telemetry Shutting Down...");
    }

}
