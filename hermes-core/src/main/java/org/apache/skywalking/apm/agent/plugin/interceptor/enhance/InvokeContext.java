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

package org.apache.skywalking.apm.agent.plugin.interceptor.enhance;

import java.lang.reflect.Method;

/**
 * This is a method return value manipulator. When a interceptor's method, such as
 * {@link InstanceMethodsAroundInterceptor#beforeMethod(EnhancedInstance, Method, Object[], Class[], InvokeContext)} (org.apache.skywalking.apm.agent.core.plugin.interceptor.EnhancedClassInstanceContext,
 * has this as a method argument, the interceptor can manipulate
 * the method's return value. <p> The new value set to this object, by {@link InvokeContext#defineReturnValue(Object)},
 * will override the origin return value.
 *
 * @author wusheng
 */
public final class InvokeContext {
    private boolean isContinue = true;

    private Object ret = null;

    //这里的Span和Scope不能直接指明具体类型，需要用Object来引用。
    //如果指明具体类型，BootstrapInstrumentBoost#HIGH_PRIORITY_CLASSES 里手动加载大量的OpenTelemetry相关类。
    //因此，为了方便，这里使用Object来引用，通过InvokeContextUtil来操作Span和Scope.
    private Object span;
    //    private Deque<Object> scopes;
    private Object scope;

    private Object context;

    /**
     * define the new return value.
     *
     * @param ret new return value.
     */
    public void defineReturnValue(Object ret) {
        this.isContinue = false;
        this.ret = ret;
    }

    /**
     * @return true, will trigger method interceptor({@link InstMethodsInter} and {@link StaticMethodsInter}) to invoke
     * the origin method. Otherwise, not.
     */
    public boolean isContinue() {
        return isContinue;
    }

    /**
     * @return the new return value.
     */
    public Object _ret() {
        return ret;
    }

    public final InvokeContext span(Object span) {
        this.span = span;
        return this;
    }

    public final InvokeContext scope(Object scope) {
//        if (this.scopes == null)
//            this.scopes = new ArrayDeque<>(2);
//
//        this.scopes.addLast(scope);

        this.scope = scope;
        return this;
    }


    public final Object span() {
        return this.span;
    }

//
//    public final Deque<Object> scopes() {
//        return this.scopes;
//    }

    public final Object scope() {
        return this.scope;
    }


    public final InvokeContext context(Object context) {
        this.context = context;
        return this;
    }

    public final Object context() {
        return this.context;
    }
}
