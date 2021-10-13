package org.apache.skywalking.apm.agent.boot;

/**
 * @author daozhang
 * @apiNote AgentRegisterException
 * @since 2020/5/20
 */
public class AgentRegisterException extends RuntimeException {
    public AgentRegisterException(String message) {
        super(message);
    }
}
