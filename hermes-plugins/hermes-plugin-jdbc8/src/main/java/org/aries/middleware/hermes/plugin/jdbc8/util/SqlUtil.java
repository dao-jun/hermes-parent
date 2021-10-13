package org.aries.middleware.hermes.plugin.jdbc8.util;

import cn.hutool.core.util.StrUtil;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author daozhang
 * @apiNote SqlJudgeUtil
 * @since 2020/9/1
 */
public class SqlUtil {
    private static final ILog log = LogManager.getLogger(SqlUtil.class);

    public static boolean isSql(String sql) {
        if (StrUtil.isBlank(sql)) {
            //this could not happen. unless unexpected error occurs.
            log.info("sql is blank. ignore...");
            return false;
        }


        if ((sql.contains("select") || sql.contains("SELECT")) && (sql.contains("from") || sql.contains("FROM")))
            return true;

        if ((sql.contains("update") || sql.contains("UPDATE")) && (sql.contains("set") || sql.contains("SET")))
            return true;

        if ((sql.contains("insert") || sql.contains("INSERT")) && (sql.contains("into") || sql.contains("INTO")))
            return true;

        return
                (sql.contains("delete") || sql.contains("DELETE")) && sql.contains("from") || sql.contains("FROM");

    }


    public static Set<String> setters = new HashSet<>(Arrays.asList("setArray", "setBigDecimal", "setBoolean", "setByte", "setDate",
            "setDouble", "setFloat", "setInt", "setLong", "setNString", "setObject", "setRowId", "setShort", "setString",
            "setTime", "setTimestamp", "setURL"));


    public static String sqlCommand(String sql) {
        int start = 0;
        for (int a = 0; a < sql.length(); a++) {
            char c = sql.charAt(a);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                start = a;
                break;
            }
        }

        return sql.substring(start, start + 6).toLowerCase();
    }

    public static boolean isSelect(String sql) {
        if (StrUtil.isBlank(sql) || sql.length() <= 6)
            return false;

        return sqlCommand(sql).equals("select");
    }
}
