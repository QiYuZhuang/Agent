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

package org.dbiir.harp.executor.sql.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.dbiir.harp.utils.router.context.RouteMapper;

import java.util.Collections;
import java.util.List;

/**
 * SQL unit.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "sql")
@ToString
public final class SQLUnit {
    
    private String sql;
    
    private List<String> tables;
    
    private List<Object> parameters;

    private List<RouteMapper> tableRouteMappers;

    public SQLUnit(String sql, List<Object> params) {
        this.sql = sql;
        this.parameters = params;
        this.tableRouteMappers = Collections.emptyList();
    }

    public SQLUnit(String sql, List<Object> params, List<RouteMapper> routers) {
        this.sql = sql;
        this.parameters = params;
        this.tableRouteMappers = routers;
    }
    
    public void CombineSQLUnit(SQLUnit other) {
        sql = sql + ";" + other.getSql();
        parameters.addAll(other.getParameters());
    }

    public boolean isLastQuery() {
        return sql.contains("/*last query*/");
    }

    public boolean isOnePhaseQuery() {
        return sql.contains("/*last one phase query*/");
    }
}
