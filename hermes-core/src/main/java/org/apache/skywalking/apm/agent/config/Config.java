package org.apache.skywalking.apm.agent.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.skywalking.apm.agent.logging.LogLevel;
import org.apache.skywalking.apm.agent.logging.LogOutput;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is the core config in sniffer agent.
 *
 * @author wusheng
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {


    public static boolean AGENT_OPEN_DEBUG = true;
    public static boolean APP_STARTED = false;

    public static class Logging {

        public static String FILE_NAME = "apm.log";

        public static String DIR = "/data/logs/" + Application.NAME + "/apm";

        public static int MAX_FILE_SIZE = 300 * 1024 * 1024;

        public static int MAX_HISTORY_FILES = 5;

        public static LogLevel LEVEL = LogLevel.DEBUG;

        public static LogOutput OUTPUT = LogOutput.FILE;

        public static String PATTERN = "%level %timestamp %thread %class : %msg %throwable";
    }


    public static class Application {
        public static final String NAME = System.getProperty("octopus.app.name", "unknown");
        public static final String ENV = System.getProperty("octopus.app.env", "unknown");
    }

    public static class Plugin {

        public static int PEER_MAX_LENGTH = 128;

        public static class JdkThreading {

            public static Collection<String> THREAD_CLZ_PREFIXES = Arrays.asList(
                    "org.apache.shardingsphere.core.execute.engine.ShardingExecuteEngine",
                    "com.dangdang.ddframe.rdb.sharding.executor.");
        }

    }
}
