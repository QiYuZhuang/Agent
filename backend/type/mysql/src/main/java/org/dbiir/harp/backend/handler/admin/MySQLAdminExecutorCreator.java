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

package org.dbiir.harp.backend.handler.admin;




import org.dbiir.harp.backend.context.ProxyContext;
import org.dbiir.harp.backend.handler.admin.executor.*;
import org.dbiir.harp.utils.binder.statement.SQLStatementContext;
import org.dbiir.harp.utils.common.segment.dml.item.ExpressionProjectionSegment;
import org.dbiir.harp.utils.common.segment.dml.item.ProjectionSegment;
import org.dbiir.harp.utils.common.statement.SQLStatement;
import org.dbiir.harp.utils.common.statement.dal.SetStatement;
import org.dbiir.harp.utils.common.statement.dal.UseStatement;
import org.dbiir.harp.utils.common.statement.dml.SelectStatement;
import org.dbiir.harp.utils.common.statement.mysql.dal.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Database admin executor creator for MySQL.
 */
public final class MySQLAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final String INFORMATION_SCHEMA = "information_schema";
    
    private static final String PERFORMANCE_SCHEMA = "performance_schema";
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLShowFunctionStatusStatement) {
            return Optional.of(new ShowFunctionStatusExecutor((MySQLShowFunctionStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcedureStatusStatement) {
            return Optional.of(new ShowProcedureStatusExecutor((MySQLShowProcedureStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            return Optional.of(new ShowTablesExecutor((MySQLShowTablesStatement) sqlStatement, sqlStatementContext.getDatabaseType()));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext, final String sql, final String databaseName) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof UseStatement) {
            return Optional.of(new UseDatabaseExecutor((UseStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return Optional.of(new ShowDatabasesExecutor((MySQLShowDatabasesStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcessListStatement) {
            return Optional.of(new ShowProcessListExecutor());
        }
        if (sqlStatement instanceof MySQLKillStatement) {
            return Optional.of(new KillProcessExecutor((MySQLKillStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowCreateDatabaseStatement) {
            return Optional.of(new ShowCreateDatabaseExecutor((MySQLShowCreateDatabaseStatement) sqlStatement));
        }
        if (sqlStatement instanceof SetStatement) {
            return Optional.of(new MySQLSetVariableAdminExecutor((SetStatement) sqlStatement));
        }
        if (sqlStatement instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) sqlStatement;
            if (null == selectStatement.getFrom()) {
                return getSelectFunctionOrVariableExecutor(selectStatement, sql, databaseName);
            }
            if (isQueryInformationSchema(databaseName)) {
                return MySQLInformationSchemaExecutorFactory.newInstance(selectStatement, sql);
            }
            if (isQueryPerformanceSchema(databaseName)) {
                // TODO
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Optional<DatabaseAdminExecutor> getSelectFunctionOrVariableExecutor(final SelectStatement selectStatement, final String sql, final String databaseName) {
        if (isShowSpecialFunction(selectStatement, ShowConnectionIdExecutor.FUNCTION_NAME)) {
            return Optional.of(new ShowConnectionIdExecutor(selectStatement));
        }
        if (isShowSpecialFunction(selectStatement, ShowVersionExecutor.FUNCTION_NAME)) {
            return Optional.of(new ShowVersionExecutor(selectStatement));
        }
        if (isShowSpecialFunction(selectStatement, ShowCurrentUserExecutor.FUNCTION_NAME)
                || isShowSpecialFunction(selectStatement, ShowCurrentUserExecutor.FUNCTION_NAME_ALIAS)) {
            return Optional.of(new ShowCurrentUserExecutor());
        }
        boolean hasNoResource = hasNoResource();
        if (hasNoResource && isShowSpecialFunction(selectStatement, ShowTransactionExecutor.TRANSACTION_READ_ONLY)) {
            return Optional.of(new ShowTransactionExecutor(ShowTransactionExecutor.TRANSACTION_READ_ONLY));
        }
        if (hasNoResource && isShowSpecialFunction(selectStatement, ShowTransactionExecutor.TRANSACTION_ISOLATION)) {
            return Optional.of(new ShowTransactionExecutor(ShowTransactionExecutor.TRANSACTION_ISOLATION));
        }
        if (isShowSpecialFunction(selectStatement, ShowCurrentDatabaseExecutor.FUNCTION_NAME)) {
            return Optional.of(new ShowCurrentDatabaseExecutor());
        }
        return mockExecutor(databaseName, selectStatement, sql);
    }
    
    private boolean isShowSpecialFunction(final SelectStatement sqlStatement, final String functionName) {
        Iterator<ProjectionSegment> segmentIterator = sqlStatement.getProjections().getProjections().iterator();
        ProjectionSegment firstProjection = segmentIterator.next();
        return !segmentIterator.hasNext() && firstProjection instanceof ExpressionProjectionSegment
                && functionName.equalsIgnoreCase(((ExpressionProjectionSegment) firstProjection).getText());
    }
    
    private boolean isQueryInformationSchema(final String databaseName) {
        // TODO remove DefaultDatabaseMetaDataExecutor when sql federation can support all system table query
        return INFORMATION_SCHEMA.equalsIgnoreCase(databaseName) && !ProxyContext.getInstance().getDatabase(databaseName).isComplete();
    }
    
    private boolean isQueryPerformanceSchema(final String databaseName) {
        return PERFORMANCE_SCHEMA.equalsIgnoreCase(databaseName);
    }
    
    private Optional<DatabaseAdminExecutor> mockExecutor(final String databaseName, final SelectStatement sqlStatement, final String sql) {
        boolean isNotUseSchema = null == databaseName && null == sqlStatement.getFrom();
        if (hasNoResource()) {
            return Optional.of(new NoResourceShowExecutor(sqlStatement));
        }
        return isNotUseSchema ? Optional.of(new UnicastResourceShowExecutor(sqlStatement, sql)) : Optional.empty();
    }
    
    private boolean hasNoResource() {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        if (databaseNames.isEmpty()) {
            return true;
        }
        for (String each : databaseNames) {
            if (ProxyContext.getInstance().getDatabase(each).containsDataSource()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
