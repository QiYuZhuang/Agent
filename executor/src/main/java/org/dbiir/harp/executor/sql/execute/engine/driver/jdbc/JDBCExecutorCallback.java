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
 */

package org.dbiir.harp.executor.sql.execute.engine.driver.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.dbiir.harp.executor.kernel.model.ExecutorCallback;
import org.dbiir.harp.executor.sql.context.ExecutionUnit;
import org.dbiir.harp.executor.sql.context.SQLUnit;
import org.dbiir.harp.executor.sql.execute.engine.ConnectionMode;
import org.dbiir.harp.executor.sql.execute.engine.SQLExecutionUnit;
import org.dbiir.harp.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.dbiir.harp.executor.sql.hook.SPISQLExecutionHook;
import org.dbiir.harp.executor.sql.hook.SQLExecutionHook;
import org.dbiir.harp.executor.sql.process.ExecuteProcessEngine;
import org.dbiir.harp.utils.common.database.metadata.DataSourceMetaData;
import org.dbiir.harp.utils.common.database.type.DatabaseType;
import org.dbiir.harp.utils.common.statement.SQLStatement;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC executor callback.
 *
 * @param <T> class type of return value
 */
@RequiredArgsConstructor
public abstract class JDBCExecutorCallback<T> implements ExecutorCallback<JDBCExecutionUnit, T> {
    
    private static final Map<String, DataSourceMetaData> CACHED_DATASOURCE_METADATA = new ConcurrentHashMap<>();
    
    private final DatabaseType protocolType;
    
    private final Map<String, DatabaseType> storageTypes;
    
    private final SQLStatement sqlStatement;
    
    private final boolean isExceptionThrown;
    
    @Override
    public final Collection<T> execute(final Collection<JDBCExecutionUnit> executionUnits, final boolean isTrunkThread) throws SQLException {
        // TODO It is better to judge whether need sane result before execute, can avoid exception thrown
        Collection<T> result = new LinkedList<>();
        for (JDBCExecutionUnit each : executionUnits) {
            T executeResult = execute(each, isTrunkThread);
            if (null != executeResult) {
                result.add(executeResult);
            }
        }
        return result;
    }
    
    /*
     * To make sure SkyWalking will be available at the next release of ShardingSphere, a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    @SneakyThrows
    private T execute(final JDBCExecutionUnit jdbcExecutionUnit, final boolean isTrunkThread) throws SQLException {
        SQLExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        DatabaseType storageType = storageTypes.get(jdbcExecutionUnit.getExecutionUnit().getDataSourceName());
        DataSourceMetaData dataSourceMetaData = getDataSourceMetaData(jdbcExecutionUnit.getStorageResource().getConnection().getMetaData(), storageType);
        SQLExecutionHook sqlExecutionHook = new SPISQLExecutionHook();
        try {
            ExecutionUnit executionUnit = jdbcExecutionUnit.getExecutionUnit();
            SQLUnit sqlUnit = executionUnit.getSqlUnit();
            sqlExecutionHook.start(jdbcExecutionUnit.getExecutionUnit().getDataSourceName(), sqlUnit.getSql(), sqlUnit.getParameters(), dataSourceMetaData, isTrunkThread);
            T result = executeSQL(sqlUnit.getSql(), jdbcExecutionUnit.getStorageResource(), jdbcExecutionUnit.getConnectionMode(), storageType);
            sqlExecutionHook.finishSuccess();
            finishReport(jdbcExecutionUnit);
            return result;
        } catch (final SQLException ex) {
            if (!storageType.equals(protocolType)) {
                Optional<T> saneResult = getSaneResult(sqlStatement, ex);
                if (saneResult.isPresent()) {
                    return isTrunkThread ? saneResult.get() : null;
                }
            }
            sqlExecutionHook.finishFailure(ex);
            SQLExecutorExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    private DataSourceMetaData getDataSourceMetaData(final DatabaseMetaData databaseMetaData, final DatabaseType storageType) throws SQLException {
        String url = databaseMetaData.getURL();
        if (CACHED_DATASOURCE_METADATA.containsKey(url)) {
            return CACHED_DATASOURCE_METADATA.get(url);
        }
        DataSourceMetaData result = storageType.getDataSourceMetaData(url, databaseMetaData.getUserName());
        CACHED_DATASOURCE_METADATA.put(url, result);
        return result;
    }
    
    private void finishReport(final SQLExecutionUnit executionUnit) {
        new ExecuteProcessEngine().finishExecution(executionUnit);
    }
    
    protected abstract T executeSQL(String sql, Statement statement, ConnectionMode connectionMode, DatabaseType storageType) throws SQLException;
    
    protected abstract Optional<T> getSaneResult(SQLStatement sqlStatement, SQLException ex);
}
