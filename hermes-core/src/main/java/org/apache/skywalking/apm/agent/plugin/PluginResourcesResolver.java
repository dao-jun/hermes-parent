package org.apache.skywalking.apm.agent.plugin;

import org.apache.skywalking.apm.agent.logging.api.ILog;
import org.apache.skywalking.apm.agent.logging.api.LogManager;
import org.apache.skywalking.apm.agent.plugin.loader.AgentClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class PluginResourcesResolver {
    private static final ILog logger = LogManager.getLogger(PluginResourcesResolver.class);

    public List<URL> getResources() {
        List<URL> cfgUrlPaths = new ArrayList<>();
        Enumeration<URL> urls;
        try {
            urls = AgentClassLoader.getDefault().getResources("apm-plugin.def");

            while (urls.hasMoreElements()) {
                URL pluginUrl = urls.nextElement();
                cfgUrlPaths.add(pluginUrl);
                logger.info("find apm plugin define in {}", pluginUrl);
            }

            return cfgUrlPaths;
        } catch (IOException e) {
            logger.error("read resources failure.", e);
        }
        return null;
    }
}
