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

package org.dbiir.harp.merger.dql;

import lombok.RequiredArgsConstructor;
import org.dbiir.harp.executor.sql.execute.result.query.QueryResult;
import org.dbiir.harp.merger.common.IteratorStreamMergedResult;
import org.dbiir.harp.merger.dql.groupby.GroupByMemoryMergedResult;
import org.dbiir.harp.merger.dql.groupby.GroupByStreamMergedResult;
import org.dbiir.harp.merger.dql.orderby.OrderByStreamMergedResult;
import org.dbiir.harp.merger.dql.pagination.LimitDecoratorMergedResult;
import org.dbiir.harp.merger.merger.ResultMerger;
import org.dbiir.harp.merger.result.MergedResult;
import org.dbiir.harp.utils.binder.segment.select.orderby.OrderByItem;
import org.dbiir.harp.utils.binder.segment.select.pagination.PaginationContext;
import org.dbiir.harp.utils.binder.statement.SQLStatementContext;
import org.dbiir.harp.utils.binder.statement.dml.SelectStatementContext;
import org.dbiir.harp.utils.common.database.type.DatabaseType;
import org.dbiir.harp.utils.common.database.type.DatabaseTypeEngine;
import org.dbiir.harp.utils.common.enums.NullsOrderType;
import org.dbiir.harp.utils.common.enums.OrderDirection;
import org.dbiir.harp.utils.common.metadata.database.AgentDatabase;
import org.dbiir.harp.utils.common.metadata.database.schema.model.AgentSchema;
import org.dbiir.harp.utils.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.dbiir.harp.utils.common.statement.dml.SelectStatement;
import org.dbiir.harp.utils.common.util.SQLUtils;
import org.dbiir.harp.utils.context.ConnectionContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DQL result merger for Sharding.
 */
@RequiredArgsConstructor
public final class ShardingDQLResultMerger implements ResultMerger {
    
    private final DatabaseType protocolType;
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext<?> sqlStatementContext,
                              final AgentDatabase database, final ConnectionContext connectionContext) throws SQLException {
        if (1 == queryResults.size() && !isNeedAggregateRewrite(sqlStatementContext)) {
            return new IteratorStreamMergedResult(queryResults);
        }
        Map<String, Integer> columnLabelIndexMap = getColumnLabelIndexMap(queryResults.get(0));
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        selectStatementContext.setIndexes(columnLabelIndexMap);
        MergedResult mergedResult = build(queryResults, selectStatementContext, columnLabelIndexMap, database);
        return decorate(queryResults, selectStatementContext, mergedResult);
    }
    
    private boolean isNeedAggregateRewrite(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isNeedAggregateRewrite();
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = queryResult.getMetaData().getColumnCount(); i > 0; i--) {
            result.put(SQLUtils.getExactlyValue(queryResult.getMetaData().getColumnLabel(i)), i);
        }
        return result;
    }
    
    private MergedResult build(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                               final Map<String, Integer> columnLabelIndexMap, final AgentDatabase database) throws SQLException {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(selectStatementContext.getDatabaseType(), database.getName());
        AgentSchema schema = selectStatementContext.getTablesContext().getSchemaName()
                .map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        if (isNeedProcessGroupBy(selectStatementContext)) {
            return getGroupByMergedResult(queryResults, selectStatementContext, columnLabelIndexMap, schema);
        }
        if (isNeedProcessDistinctRow(selectStatementContext)) {
            setGroupByForDistinctRow(selectStatementContext);
            return getGroupByMergedResult(queryResults, selectStatementContext, columnLabelIndexMap, schema);
        }
        if (isNeedProcessOrderBy(selectStatementContext)) {
            return new OrderByStreamMergedResult(queryResults, selectStatementContext, schema);
        }
        return new IteratorStreamMergedResult(queryResults);
    }
    
    private boolean isNeedProcessGroupBy(final SelectStatementContext selectStatementContext) {
        return !selectStatementContext.getGroupByContext().getItems().isEmpty() || !selectStatementContext.getProjectionsContext().getAggregationProjections().isEmpty();
    }
    
    private boolean isNeedProcessDistinctRow(final SelectStatementContext selectStatementContext) {
        return selectStatementContext.getProjectionsContext().isDistinctRow();
    }
    
    private void setGroupByForDistinctRow(final SelectStatementContext selectStatementContext) {
        for (int index = 1; index <= selectStatementContext.getProjectionsContext().getExpandProjections().size(); index++) {
            OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(-1, -1, index, OrderDirection.ASC, createDefaultNullsOrderType(selectStatementContext.getSqlStatement())));
            orderByItem.setIndex(index);
            selectStatementContext.getGroupByContext().getItems().add(orderByItem);
        }
    }
    
    private NullsOrderType createDefaultNullsOrderType(final SelectStatement selectStatement) {
        return NullsOrderType.FIRST;
    }
    
    private MergedResult getGroupByMergedResult(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                                                final Map<String, Integer> columnLabelIndexMap, final AgentSchema schema) throws SQLException {
        return selectStatementContext.isSameGroupByAndOrderByItems()
                ? new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectStatementContext, schema)
                : new GroupByMemoryMergedResult(queryResults, selectStatementContext, schema);
    }
    
    private boolean isNeedProcessOrderBy(final SelectStatementContext selectStatementContext) {
        return !selectStatementContext.getOrderByContext().getItems().isEmpty();
    }
    
    private MergedResult decorate(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final MergedResult mergedResult) throws SQLException {
        PaginationContext paginationContext = selectStatementContext.getPaginationContext();
        if (!paginationContext.isHasPagination() || 1 == queryResults.size()) {
            return mergedResult;
        }
        String trunkDatabaseName = DatabaseTypeEngine.getTrunkDatabaseType(protocolType.getType()).getType();
        if ("MySQL".equals(trunkDatabaseName) || "PostgreSQL".equals(trunkDatabaseName) || "openGauss".equals(trunkDatabaseName)) {
            return new LimitDecoratorMergedResult(mergedResult, paginationContext);
        }
//        if ("Oracle".equals(trunkDatabaseName)) {
//            return new RowNumberDecoratorMergedResult(mergedResult, paginationContext);
//        }
//        if ("SQLServer".equals(trunkDatabaseName)) {
//            return new TopAndRowNumberDecoratorMergedResult(mergedResult, paginationContext);
//        }
        return mergedResult;
    }
}
