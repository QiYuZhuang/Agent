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

package org.dbiir.harp.backend.connector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dbiir.harp.backend.context.ProxyContext;
import org.dbiir.harp.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.dbiir.harp.utils.binder.QueryContext;
import org.dbiir.harp.utils.common.metadata.database.AgentDatabase;

/**
 * Database connector factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseConnectorFactory {
    
    private static final DatabaseConnectorFactory INSTANCE = new DatabaseConnectorFactory();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static DatabaseConnectorFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of {@link DatabaseConnector}.
     *
     * @param queryContext query context
     * @param backendConnection backend connection
     * @param preferPreparedStatement use prepared statement as possible
     * @return created instance
     */
    public DatabaseConnector newInstance(final QueryContext queryContext, final BackendConnection backendConnection, final boolean preferPreparedStatement) {
        AgentDatabase database = ProxyContext.getInstance().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        String driverType = preferPreparedStatement || !queryContext.getParameters().isEmpty() ? JDBCDriverType.PREPARED_STATEMENT : JDBCDriverType.STATEMENT;
        DatabaseConnector result = new DatabaseConnector(driverType, database, queryContext, backendConnection);
        backendConnection.add(result);
        return result;
    }
}
