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

package org.dbiir.harp.utils.binder.statement.dal;

import lombok.Getter;
import org.dbiir.harp.utils.binder.segment.table.TablesContext;
import org.dbiir.harp.utils.binder.statement.CommonSQLStatementContext;
import org.dbiir.harp.utils.binder.type.TableAvailable;
import org.dbiir.harp.utils.common.segment.generic.table.SimpleTableSegment;
import org.dbiir.harp.utils.common.statement.mysql.dal.MySQLShowCreateTableStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Show create table statement context.
 */
@Getter
public final class ShowCreateTableStatementContext extends CommonSQLStatementContext<MySQLShowCreateTableStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    public ShowCreateTableStatementContext(final MySQLShowCreateTableStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable(), getDatabaseType());
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return null == getSqlStatement().getTable() ? Collections.emptyList() : Collections.singletonList(getSqlStatement().getTable());
    }
}
