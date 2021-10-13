package org.aries.middleware.hermes.plugin.jdbc8.instrumentations;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.match.ClassMatch;
import org.aries.middleware.hermes.plugin.jdbc8.util.SqlUtil;

import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static org.apache.skywalking.apm.agent.plugin.match.NameMatch.byName;

/**
 * @author daozhang
 * @apiNote PreparedStatementInstrumentation
 * @since 2020/5/21
 */
public class PreparedStatementInstrumentation extends AbstractMysqlInstrumentation {


    @Override
    protected ClassMatch enhanceClass() {
        return byName("com.mysql.cj.jdbc.ClientPreparedStatement");
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("execute")
                                .or(named("executeUpdate"))
                                .or(named("executeQuery"))
                                .or(named("executeLargeUpdate"));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "org.aries.middleware.hermes.plugin.jdbc8.PrepareStatementInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },

                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        ElementMatcher.Junction<MethodDescription> matcher = none();
                        Set<String> setters = SqlUtil.setters;
                        for (String setter : setters) {
                            matcher = matcher.or(named(setter));
                        }

                        return matcher;
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "org.aries.middleware.hermes.plugin.jdbc8.PreparedStatementSetterInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
