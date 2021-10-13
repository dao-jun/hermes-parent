package org.aries.middleware.hermes.plugin.jdbc8.info;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author daozhang
 * @apiNote StatementEnhanceInfo
 * @since 2020/5/21
 */
@Data
@Accessors(chain = true)
public class StatementEnhanceInfo {
    private ConnectionInfo connectionInfo;
    private String statementName;
    private String sql;

}
