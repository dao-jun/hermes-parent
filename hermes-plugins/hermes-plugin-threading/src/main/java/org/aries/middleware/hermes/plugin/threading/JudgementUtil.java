package org.aries.middleware.hermes.plugin.threading;

import org.apache.skywalking.apm.agent.config.Config;
import org.aries.middleware.hermes.plugin.threading.command.CallableCommand;
import org.aries.middleware.hermes.plugin.threading.command.RunnableCommand;

import java.util.Objects;

/**
 * @author daozhang
 * @apiNote JudgementUtil
 * @since 2020/6/3
 */
public class JudgementUtil {

    public static boolean judge(Object target) {

        if (Objects.isNull(target))
            return false;

        if (target instanceof CallableCommand || target instanceof RunnableCommand)
            return false;

        String name = target.getClass().getName();
        if (name.startsWith("org.aries.middleware.hermes"))
            return false;

        for (String prefix : Config.Plugin.JdkThreading.THREAD_CLZ_PREFIXES) {
            if (name.startsWith(prefix))
                return true;
        }

        return false;
    }

}
