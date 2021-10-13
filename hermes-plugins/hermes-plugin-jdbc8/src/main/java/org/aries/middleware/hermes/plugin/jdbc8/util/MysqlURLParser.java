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
 * {@link MysqlURLParser} parse connection url of mysql.
 */
public class MysqlURLParser extends AbstractURLParser {

    private static final int DEFAULT_PORT = 3306;
    private String dbType = "Mysql";

    public MysqlURLParser(String url) {
        super(url);
    }

    public MysqlURLParser(String url, String dbType) {
        super(url);
        this.dbType = dbType;
    }

    @Override
    protected URLLocation fetchDatabaseHostsIndexRange() {
        int hostLabelStartIndex = url.indexOf("//");
        int hostLabelEndIndex = url.indexOf("/", hostLabelStartIndex + 2);
        if (hostLabelEndIndex == -1) {
            hostLabelEndIndex = url.indexOf("?", hostLabelStartIndex + 2);
        }
        return new URLLocation(hostLabelStartIndex + 2, hostLabelEndIndex);
    }

    protected String fetchDatabaseNameFromURL(int startSize) {
        URLLocation hostsLocation = fetchDatabaseNameIndexRange(startSize);
        if (hostsLocation == null) {
            return "";
        }
        return url.substring(hostsLocation.startIndex(), hostsLocation.endIndex());
    }

    protected URLLocation fetchDatabaseNameIndexRange(int startSize) {
        int databaseStartTag = url.indexOf("/", startSize);
        if (databaseStartTag == -1) {
            return null;
        }
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    protected URLLocation fetchDatabaseNameIndexRange() {
        int databaseStartTag = url.lastIndexOf("/");
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    public ConnectionInfo parse() {
        URLLocation location = fetchDatabaseHostsIndexRange();
        String hosts = url.substring(location.startIndex(), location.endIndex());
        String[] hostSegment = hosts.split(",");
        if (hostSegment.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String host : hostSegment) {
                if (host.split(":").length == 1) {
                    sb.append(host).append(":").append(DEFAULT_PORT).append(",");
                } else {
                    sb.append(host).append(",");
                }
            }
            return new ConnectionInfo(dbType, sb.substring(0, sb.length() - 1), fetchDatabaseNameFromURL());
        } else {
            String[] hostAndPort = hostSegment[0].split(":");
            if (hostAndPort.length != 1) {
                return new ConnectionInfo(dbType, hostAndPort[0], Integer.valueOf(hostAndPort[1]), fetchDatabaseNameFromURL(location
                        .endIndex()));
            } else {
                return new ConnectionInfo(dbType, hostAndPort[0], DEFAULT_PORT, fetchDatabaseNameFromURL(location
                        .endIndex()));
            }
        }
    }

}
