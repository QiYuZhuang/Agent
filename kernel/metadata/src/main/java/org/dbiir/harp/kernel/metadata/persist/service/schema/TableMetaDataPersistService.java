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

package org.dbiir.harp.kernel.metadata.persist.service.schema;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.dbiir.harp.kernel.metadata.persist.node.DatabaseMetaDataNode;
import org.dbiir.harp.mode.spi.PersistRepository;
import org.dbiir.harp.utils.common.metadata.database.schema.model.AgentTable;
import org.dbiir.harp.utils.common.yaml.YamlEngine;
import org.dbiir.harp.utils.common.yaml.schema.pojo.YamlAgentTable;
import org.dbiir.harp.utils.common.yaml.schema.swapper.YamlTableSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Table meta data persist service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistService implements SchemaMetaDataPersistService<Map<String, AgentTable>> {
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Map<String, AgentTable> tables) {
        tables.forEach((key, value) -> repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, key.toLowerCase()),
                YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(value))));
    }
    
    @Override
    public Map<String, AgentTable> load(final String databaseName, final String schemaName) {
        Collection<String> tableNames = repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName));
        return tableNames.isEmpty() ? Collections.emptyMap() : getTableMetaDataByTableNames(databaseName, schemaName, tableNames);
    }
    
    @Override
    public void delete(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, tableName.toLowerCase()));
    }
    
    private Map<String, AgentTable> getTableMetaDataByTableNames(final String databaseName, final String schemaName, final Collection<String> tableNames) {
        Map<String, AgentTable> result = new LinkedHashMap<>(tableNames.size(), 1);
        tableNames.forEach(each -> {
            String table = repository.getDirectly(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, each));
            if (!Strings.isNullOrEmpty(table)) {
                result.put(each.toLowerCase(), new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(table, YamlAgentTable.class)));
            }
        });
        return result;
    }
}