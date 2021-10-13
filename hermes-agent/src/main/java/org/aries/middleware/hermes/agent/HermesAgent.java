package org.aries.middleware.hermes.agent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.apache.skywalking.apm.agent.boot.AgentPackageNotFoundException;
import org.apache.skywalking.apm.agent.boot.ServiceManager;
import org.apache.skywalking.apm.agent.config.Config;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.*;
import org.apache.skywalking.apm.agent.plugin.bootstrap.BootstrapInstrumentBoost;
import org.apache.skywalking.apm.agent.plugin.jdk9module.JDK9ModuleExporter;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class HermesAgent {

    private static final ILog logger = LogManager.getLogger(HermesAgent.class);

    /**
     * Main entrance. Use byte-buddy transform to enhance all classes, which define in plugins.
     *
     * @param agentArgs
     * @param instrumentation
     * @throws PluginException
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) throws PluginException {
        String appName = Config.Application.NAME;
        if (StrUtil.isBlank(appName)) {
            System.out.println("app name is blank, apm agent start cancelling...");
            return;
        }

        final PluginFinder pluginFinder;
        ServiceManager.INSTANCE.prepare();

        try {
            pluginFinder = new PluginFinder(new PluginBootstrap().loadPlugins());
        } catch (AgentPackageNotFoundException ape) {
            logger.error(ape, "Locate agent.jar failure. Shutting down.");
            return;
        } catch (Exception e) {
            logger.error(e, "apm agent initialized failure. Shutting down.");
            return;
        }

        final ByteBuddy byteBuddy = new ByteBuddy()
                .with(TypeValidation.of(Config.AGENT_OPEN_DEBUG));

        AgentBuilder agentBuilder = new AgentBuilder.Default(byteBuddy)
                .ignore(
                        nameStartsWith("net.bytebuddy.")
                                .or(nameStartsWith("org.slf4j."))
                                .or(nameStartsWith("org.groovy."))
                                .or(nameContains("javassist"))
                                .or(nameContains(".asm."))
                                .or(nameContains(".reflectasm."))
                                .or(nameStartsWith("sun.reflect"))
                                .or(allApmAgentExcludeToolkit())
                                .or(ElementMatchers.isSynthetic()));

        JDK9ModuleExporter.EdgeClasses edgeClasses = new JDK9ModuleExporter.EdgeClasses();

        try {
            agentBuilder = BootstrapInstrumentBoost.inject(pluginFinder, instrumentation, agentBuilder, edgeClasses);
        } catch (Exception e) {
            logger.error(e, "apm agent inject bootstrap instrumentation failure. Shutting down.");
            return;
        }

        try {
            agentBuilder = JDK9ModuleExporter.openReadEdge(instrumentation, agentBuilder, edgeClasses);
        } catch (Exception e) {
            logger.error(e, "apm agent open read edge in JDK 9+ failure. Shutting down.");
            return;
        }

        agentBuilder
                .type(pluginFinder.buildMatch())
                .transform(new Transformer(pluginFinder))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new Listener())
                .installOn(instrumentation);

        ClassLoader loader = HermesAgent.class.getClassLoader();


        /**
         * 由于部分类加载byte buddy 会监听不到导致无法增强，在此处提前触发类加载
         */
        logger.info("准备加载[java.util.concurrent.ThreadPoolExecutor,java.util.concurrent.ForkJoinTask],agentClassLoader:{}", loader);
        try {
            Class.forName("java.util.concurrent.ThreadPoolExecutor");
            Class.forName("java.util.concurrent.ForkJoinTask");
            logger.info("加载[java.util.concurrent.ThreadPoolExecutor,java.util.concurrent.ForkJoinTask]成功");
        } catch (ClassNotFoundException e) {
            logger.error("加载[java.util.concurrent.ThreadPoolExecutor,java.util.concurrent.ForkJoinTask]失败", e);
        }

        try {
            ServiceManager.INSTANCE.boot();

        } catch (Exception e) {
            logger.error(e, "apm agent boot failure.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(ServiceManager.INSTANCE::shutdown, "apm service shutdown thread"));
    }

    private static ElementMatcher.Junction<NamedElement> allApmAgentExcludeToolkit() {
        return nameStartsWith("io.opentelemetry");
    }

    private static class Transformer implements AgentBuilder.Transformer {
        private final PluginFinder pluginFinder;

        Transformer(PluginFinder pluginFinder) {
            this.pluginFinder = pluginFinder;
        }

        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                ClassLoader classLoader, JavaModule module) {

            List<AbstractClassEnhancePluginDefine> pluginDefines = pluginFinder.find(typeDescription);
            logger.info("AgentBuilder.Transformer.transform,type=[{}], plugin defines:{}", typeDescription.getName(),
                    JSONUtil.toJsonStr(pluginDefines.stream().map(c -> c.getClass().getName()).collect(Collectors.toList())));

            if (!pluginDefines.isEmpty()) {
                DynamicType.Builder<?> newBuilder = builder;
                EnhanceContext context = new EnhanceContext();
                for (AbstractClassEnhancePluginDefine define : pluginDefines) {
                    DynamicType.Builder<?> possibleNewBuilder = define.define(typeDescription, newBuilder, classLoader, context);
                    if (possibleNewBuilder != null) {
                        newBuilder = possibleNewBuilder;
                    }
                }
                if (context.isEnhanced()) {
                    logger.debug("Finish the prepare stage for {}.", typeDescription.getName());
                }

                return newBuilder;
            }

            logger.debug("Matched class {}, but ignore by finding mechanism.", typeDescription.getTypeName());
            return builder;
        }
    }

    private static class Listener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                                     boolean loaded, DynamicType dynamicType) {
            logger.info("On Transformation class {}. classLoader:{}  loaded:{} ", typeDescription.getName(), classLoader, loaded);
            InstrumentDebuggingClass.INSTANCE.log(dynamicType);
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                              boolean loaded) {
            if (logger.isDebugEnable()) {
                logger.debug("On Ignored class {}. classLoader:{}  loaded:{} ", typeDescription.getName(), classLoader, loaded);
            }
        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
                            Throwable throwable) {
            logger.error(throwable, "Enhance class " + typeName + " error.  loaded:{}", loaded);
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }
    }
}
