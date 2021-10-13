package org.aries.middleware.hermes.plugin.lettuce5.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.plugin.match.HierarchyMatch.byHierarchyMatch;

/**
 * @author daozhang
 * @apiNote ByteArrayCodecInstrumentation
 * @since 2020/7/30
 */
public class RedisCodecInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    @Override
    protected ClassMatch enhanceClass() {
        return byHierarchyMatch("io.lettuce.core.codec.RedisCodec");
    }

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
                        return named("encodeKey");
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "RedisCodecInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return true;
                    }
                }
        };
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[0];
    }
}
