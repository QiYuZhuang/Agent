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

package org.dbiir.harp.backend.config.sharding.yaml.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dbiir.harp.backend.config.sharding.yaml.ShardingRuleConfiguration;
import org.dbiir.harp.backend.config.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.dbiir.harp.utils.common.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.util.Collection;
import java.util.Optional;

/**
 * Sharding rule configuration converter.
 */
// TODO Move to pipeline module
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRuleConfigurationConverter {
    
    /**
     * Find and convert sharding rule configuration from YAML.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @return sharding rule configuration
     * @throws IllegalStateException if there is no available sharding rule
     */
    public static Optional<ShardingRuleConfiguration> findAndConvertShardingRuleConfiguration(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        return findYamlShardingRuleConfiguration(yamlRuleConfigs).map(each -> new YamlShardingRuleConfigurationSwapper().swapToObject(each));
    }
    
    /**
     * Find YAML sharding rule configuration.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @return YAML sharding rule configuration
     * @throws IllegalStateException if there is no available sharding rule
     */
    public static Optional<YamlShardingRuleConfiguration> findYamlShardingRuleConfiguration(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        return yamlRuleConfigs.stream().filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(each -> (YamlShardingRuleConfiguration) each);
    }
}
