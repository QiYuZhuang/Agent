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

package org.dbiir.harp.backend.config.sharding.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.dbiir.harp.backend.config.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.dbiir.harp.backend.config.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.dbiir.harp.backend.config.sharding.yaml.ShardingRuleConfiguration;
import org.dbiir.harp.utils.common.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding rule configuration for YAML.
 */
@Getter
@Setter
public final class YamlShardingRuleConfiguration implements YamlRuleConfiguration {
    
    private Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    private Map<String, YamlShardingAutoTableRuleConfiguration> autoTables = new LinkedHashMap<>();
    
    private Collection<String> bindingTables = new LinkedList<>();

    private String defaultShardingColumn;

    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationType() {
        return ShardingRuleConfiguration.class;
    }
}
