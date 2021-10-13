package org.apache.skywalking.apm.agent.service;


import org.apache.skywalking.apm.agent.boot.BootService;
import org.apache.skywalking.apm.agent.boot.DefaultImplementor;
import org.apache.skywalking.apm.agent.config.Config;
import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;

@DefaultImplementor
public class DefaultBootService implements BootService {
    private static final ILog log = LogManager.getLogger(DefaultBootService.class);

    @Override
    public void prepare() throws Throwable {
        log.info("Boot Service Preparing...");
    }

    @Override
    public void boot() throws Throwable {
        log.info("Boot Service Booting...");
    }

    @Override
    public void onComplete() throws Throwable {
        Config.APP_STARTED = true;
        log.info("Boot Service Completing...");
    }

    @Override
    public void shutdown() throws Throwable {
        log.info("Boot Service Shutting Down...");
    }
}
