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

package org.dbiir.harp.executor.sql.execute.result.query;

import java.sql.SQLException;

/**
 * Query result meta data.
 */
public interface QueryResultMetaData {
    
    /**
     * Get column count.
     *
     * @return column count
     * @throws SQLException SQL exception
     */
    int getColumnCount() throws SQLException;
    
    /**
     * Get table name.
     *
     * @param columnIndex column index
     * @return table name
     * @throws SQLException SQL exception
     */
    String getTableName(int columnIndex) throws SQLException;
    
    /**
     * Get column name.
     *
     * @param columnIndex column index
     * @return column name
     * @throws SQLException SQL exception
     */
    String getColumnName(int columnIndex) throws SQLException;
    
    /**
     * Get column label.
     *
     * @param columnIndex column index
     * @return column label
     * @throws SQLException SQL exception
     */
    String getColumnLabel(int columnIndex) throws SQLException;
    
    /**
     * Get column type.
     *
     * @param columnIndex column index
     * @return column type
     * @throws SQLException SQL exception
     */
    int getColumnType(int columnIndex) throws SQLException;
    
    /**
     * Get column type name.
     *
     * @param columnIndex column index
     * @return column type name
     * @throws SQLException SQL exception
     */
    String getColumnTypeName(int columnIndex) throws SQLException;
    
    /**
     * Get column length.
     *
     * @param columnIndex column index
     * @return column length
     * @throws SQLException SQL exception
     */
    int getColumnLength(int columnIndex) throws SQLException;
    
    /**
     * Get decimals.
     *
     * @param columnIndex column index
     * @return decimals
     * @throws SQLException SQL exception
     */
    int getDecimals(int columnIndex) throws SQLException;
    
    /**
     * Is signed.
     *
     * @param columnIndex column index
     * @return signed or not
     * @throws SQLException SQL exception
     */
    boolean isSigned(int columnIndex) throws SQLException;
    
    /**
     * Is not null.
     *
     * @param columnIndex column index
     * @return not null or null
     * @throws SQLException SQL exception
     */
    boolean isNotNull(int columnIndex) throws SQLException;
    
    /**
     * Is auto increment.
     *
     * @param columnIndex column index
     * @return auto increment or not
     * @throws SQLException SQL exception
     */
    boolean isAutoIncrement(int columnIndex) throws SQLException;
}
