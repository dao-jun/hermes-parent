/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.agent.plugin.bootstrap.template;

import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.skywalking.apm.agent.plugin.bootstrap.IBootstrapLog;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.BootstrapInterRuntimeAssist;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.InvokeContext;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * --------CLASS TEMPLATE---------
 * <p>Author, Wu Sheng </p>
 * <p>Comment, don't change this unless you are 100% sure the agent core mechanism for bootstrap class
 * instrumentation.</p>
 * <p>Date, 24th July 2019</p>
 * -------------------------------
 * <p>
 * This class wouldn't be loaded in real env. This is a class template for dynamic class generation.
 *
 * @author wusheng
 */
public class InstanceMethodInterTemplate {
    /**
     * This field is never set in the template, but has value in the runtime.
     */
    private static String APM_TARGET_INTERCEPTOR;

    private static InstanceMethodsAroundInterceptor INTERCEPTOR;
    private static IBootstrapLog LOGGER;

    /**
     * Intercept the target instance method.
     *
     * @param obj          target class instance.
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        the origin call ref.
     * @return the return value of target instance method.
     * @throws Exception only throw exception because of zuper.call() or unexpected exception in sky-walking ( This is a
     *                   bug, if anything triggers this condition ).
     */
    @RuntimeType
    public static Object intercept(@This Object obj,
                                   @AllArguments Object[] allArguments,
                                   @SuperCall Callable<?> zuper,
                                   @Origin Method method
    ) throws Throwable {
        EnhancedInstance targetObject = (EnhancedInstance) obj;

        prepare();

        InvokeContext context = new InvokeContext();
        try {
            if (INTERCEPTOR != null) {
                INTERCEPTOR.beforeMethod(targetObject, method, allArguments, method.getParameterTypes(),
                        context);
            }
        } catch (Throwable t) {
            if (LOGGER != null) {
                LOGGER.error(t, "class[{}] before method[{}] intercept failure", obj.getClass(), method.getName());
            }
        }

        Object ret = null;
        try {
            if (!context.isContinue()) {
                ret = context._ret();
            } else {
                ret = zuper.call();
            }
        } catch (Throwable t) {
            try {
                if (INTERCEPTOR != null) {
                    INTERCEPTOR.handleMethodException(targetObject, method, allArguments, method.getParameterTypes(),
                            t, context);
                }
            } catch (Throwable t2) {
                if (LOGGER != null) {
                    LOGGER.error(t2, "class[{}] handle method[{}] exception failure", obj.getClass(), method.getName());
                }
            }
            throw t;
        } finally {
            try {
                if (INTERCEPTOR != null) {
                    ret = INTERCEPTOR.afterMethod(targetObject, method, allArguments, method.getParameterTypes(),
                            ret, context);
                }
            } catch (Throwable t) {
                if (LOGGER != null) {
                    LOGGER.error(t, "class[{}] after method[{}] intercept failure", obj.getClass(), method.getName());
                }
            }
        }

        return ret;
    }

    /**
     * Prepare the context. Link to the agent core in AppClassLoader.
     */
    private static void prepare() {
        if (INTERCEPTOR == null) {
            ClassLoader loader = BootstrapInterRuntimeAssist.getAgentClassLoader();

            if (loader != null) {
                IBootstrapLog logger = BootstrapInterRuntimeAssist.getLogger(loader, APM_TARGET_INTERCEPTOR);
                if (logger != null) {
                    LOGGER = logger;

                    INTERCEPTOR = BootstrapInterRuntimeAssist.createInterceptor(loader, APM_TARGET_INTERCEPTOR, LOGGER);
                }
            } else {
                LOGGER.error("Runtime ClassLoader not found when create {}." + APM_TARGET_INTERCEPTOR);
            }
        }
    }
}
