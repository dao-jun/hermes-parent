package org.aries.middleware.hermes.plugin.lettuce5.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType;
import static org.apache.skywalking.apm.agent.plugin.match.NameMatch.byName;

public class AbstractRedisClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("setOptions").and(takesArgumentWithType(0, "io.lettuce.core.ClientOptions"));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "AbstractRedisClientInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName("io.lettuce.core.AbstractRedisClient");
    }
}
