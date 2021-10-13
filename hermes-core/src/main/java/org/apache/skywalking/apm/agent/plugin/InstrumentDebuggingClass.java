package org.apache.skywalking.apm.agent.plugin;

import net.bytebuddy.dynamic.DynamicType;
import org.apache.skywalking.apm.agent.boot.AgentPackageNotFoundException;
import org.apache.skywalking.apm.agent.boot.AgentPackagePath;
import org.apache.skywalking.apm.agent.config.Config;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;

import java.io.File;
import java.io.IOException;

/**
 * The manipulated class output. Write the dynamic classes to the `debugging` folder, when we need to do some debug and
 * recheck.
 *
 * @author wu-sheng
 */
public enum InstrumentDebuggingClass {
    INSTANCE;

    private static final ILog logger = LogManager.getLogger(InstrumentDebuggingClass.class);
    private File debuggingClassesRootPath;

    public void log(DynamicType dynamicType) {
        if (!Config.AGENT_OPEN_DEBUG) {
            return;
        }

        /**
         * try to do I/O things in synchronized way, to avoid unexpected situations.
         */
        synchronized (INSTANCE) {
            try {
                if (debuggingClassesRootPath == null) {
                    try {
                        debuggingClassesRootPath = new File(AgentPackagePath.getPath(), "/debugging");
                        if (!debuggingClassesRootPath.exists()) {
                            debuggingClassesRootPath.mkdir();
                        }
                    } catch (AgentPackageNotFoundException e) {
                        logger.error(e, "Can't find the root path for creating /debugging folder.");
                    }
                }

                try {
                    dynamicType.saveIn(debuggingClassesRootPath);
                } catch (IOException e) {
                    logger.error(e, "Can't save class {} to file." + dynamicType.getTypeDescription().getActualName());
                }
            } catch (Throwable t) {
                logger.error(t, "Save debugging classes fail.");
            }
        }
    }
}
