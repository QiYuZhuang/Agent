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
import org.dbiir.harp.utils.common.handler.dal.FlushStatementHandler;
import org.dbiir.harp.utils.common.segment.generic.table.SimpleTableSegment;
import org.dbiir.harp.utils.common.statement.dal.FlushStatement;


import java.util.Collection;

/**
 * Flush statement context.
 */
@Getter
public final class FlushStatementContext extends CommonSQLStatementContext<FlushStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    public FlushStatementContext(final FlushStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(FlushStatementHandler.getSimpleTableSegment(sqlStatement), getDatabaseType());
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return FlushStatementHandler.getSimpleTableSegment(getSqlStatement());
    }
}
