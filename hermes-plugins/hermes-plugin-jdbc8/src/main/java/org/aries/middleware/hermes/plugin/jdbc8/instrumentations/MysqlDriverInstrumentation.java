
package org.aries.middleware.hermes.plugin.jdbc8.instrumentations;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.plugin.match.NameMatch.byName;

/**
 * @author daozhang
 * @apiNote MysqlDriverInstrumentation
 * @since 2020/5/21
 */
public class MysqlDriverInstrumentation extends AbstractMysqlInstrumentation {

    @Override
    protected ClassMatch enhanceClass() {
        return byName("com.mysql.cj.jdbc.ConnectionImpl");
    }


    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[]{
                new StaticMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("getInstance");
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "org.aries.middleware.hermes.plugin.jdbc8.MysqlDriverInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
