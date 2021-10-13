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
package org.aries.middleware.hermes.plugin.jdbc8.util;

import org.aries.middleware.hermes.plugin.jdbc8.info.ConnectionInfo;

/**
 * {@link URLParser#parser(String)} support parse the connection url, such as Mysql, Oracle, H2 Database. But there are
 * some url cannot be parsed, such as Oracle connection url with multiple host.
 */
public class URLParser {

    public static ConnectionInfo parser(String url) {
        ConnectionURLParser parser = new MysqlURLParser(url);
        return parser.parse();
    }
}
