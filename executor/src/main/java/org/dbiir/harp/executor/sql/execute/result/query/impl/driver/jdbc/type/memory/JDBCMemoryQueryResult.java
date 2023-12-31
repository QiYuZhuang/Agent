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

package org.dbiir.harp.executor.sql.execute.result.query.impl.driver.jdbc.type.memory;

import org.dbiir.harp.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.dbiir.harp.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.loader.DialectQueryResultDataRowLoader;
import org.dbiir.harp.executor.sql.execute.result.query.type.memory.AbstractMemoryQueryResult;
import org.dbiir.harp.utils.common.database.type.DatabaseType;
import org.dbiir.harp.utils.common.spi.type.typed.TypedSPILoader;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC query result for memory loading.
 */
public final class JDBCMemoryQueryResult extends AbstractMemoryQueryResult {
    
    public JDBCMemoryQueryResult(final ResultSet resultSet, final DatabaseType databaseType) throws SQLException {
        super(new JDBCQueryResultMetaData(resultSet.getMetaData()),
                TypedSPILoader.getService(DialectQueryResultDataRowLoader.class, databaseType.getType()).load(resultSet.getMetaData().getColumnCount(), resultSet));
    }
}
