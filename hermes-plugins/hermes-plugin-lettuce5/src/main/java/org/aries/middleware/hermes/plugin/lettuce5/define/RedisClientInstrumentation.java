package org.aries.middleware.hermes.plugin.lettuce5.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;

import static org.apache.skywalking.apm.agent.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType;
import static org.apache.skywalking.apm.agent.plugin.match.NameMatch.byName;

public class RedisClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {


    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[]{
                new ConstructorInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgumentWithType(1, "io.lettuce.core.RedisURI");
                    }

                    @Override
                    public String getConstructorInterceptor() {
                        return "RedisClientConstructorInterceptor";
                    }
                }
        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[0];
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName("io.lettuce.core.RedisClient");
    }
}
