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
package org.aries.middleware.hermes.plugin.jdbc8.instrumentations;

import org.apache.skywalking.apm.agent.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.plugin.interceptor.enhance.ClassEnhancePluginDefine;

public abstract class AbstractMysqlInstrumentation extends ClassEnhancePluginDefine {

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[0];
    }

    @Override
    protected String[] witnessClasses() {
        return new String[]{"com.mysql.cj.jdbc.ConnectionImpl"};
    }
}
