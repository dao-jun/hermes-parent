package org.aries.middleware.hermes.plugin.jdbc8.instrumentations;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.plugin.match.NameMatch.byName;

/**
 * @author daozhang
 * @apiNote PreparedStatementCreateInstrumentation
 * @since 2020/5/21
 */
public class PreparedStatementCreateInstrumentation extends AbstractMysqlInstrumentation {


    @Override
    protected ClassMatch enhanceClass() {
        return byName("com.mysql.jdbc.ConnectionImpl");
    }


    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("prepareStatement").or(named("clientPrepareStatement"));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "org.aries.middleware.hermes.plugin.jdbc8.PreparedStatementCreateInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
