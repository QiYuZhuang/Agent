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

package org.dbiir.harp.backend.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dbiir.harp.backend.session.ConnectionSession;
import org.dbiir.harp.executor.sql.execute.result.query.QueryResult;
import org.dbiir.harp.executor.sql.execute.result.query.QueryResultMetaData;
import org.dbiir.harp.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.dbiir.harp.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.dbiir.harp.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.dbiir.harp.merger.result.MergedResult;
import org.dbiir.harp.merger.result.impl.transparent.TransparentMergedResult;
import org.dbiir.harp.utils.common.statement.mysql.dal.MySQLShowFunctionStatusStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Show function status executor.
 */
@RequiredArgsConstructor
@Getter
public final class ShowFunctionStatusExecutor implements DatabaseAdminQueryExecutor {
    
    private final MySQLShowFunctionStatusStatement showFunctionStatusStatement;
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new TransparentMergedResult(getQueryResult());
    }
    
    private QueryResult getQueryResult() {
        return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
    }
    
    private QueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = new ArrayList<>();
        columns.add(new RawQueryResultColumnMetaData("", "Db", "Db", Types.VARCHAR, "VARCHAR", 255, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Name", "Name", Types.VARCHAR, "VARCHAR", 255, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Type", "Type", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Definer", "Definer", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Modified", "Modified", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Created", "Created", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Security_type", "Security_type", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Comment", "Comment", Types.VARCHAR, "VARCHAR", 120, 0));
        columns.add(new RawQueryResultColumnMetaData("", "character_set_client", "character_set_client", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "collation_connection", "collation_connection", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Database_Collation", "Database_Collation", Types.VARCHAR, "VARCHAR", 20, 0));
        return new RawQueryResultMetaData(columns);
    }
}
