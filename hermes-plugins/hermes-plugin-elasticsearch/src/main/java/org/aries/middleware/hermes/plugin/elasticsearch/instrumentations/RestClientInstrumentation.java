package org.aries.middleware.hermes.plugin.elasticsearch.instrumentations;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static org.apache.skywalking.apm.agent.plugin.match.NameMatch.byName;

public class RestClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    @Override
    protected ClassMatch enhanceClass() {
        return byName("org.elasticsearch.client.RestClient");
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
                        return named("performRequest")
                                .and(takesArguments(1));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "org.aries.middleware.hermes.plugin.elasticsearch.RestClientMethodInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
