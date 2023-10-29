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

package org.dbiir.harp.utils.common.yaml.config.shortcut;

import lombok.SneakyThrows;
import org.dbiir.harp.utils.common.spi.HarpServiceLoader;
import org.dbiir.harp.utils.common.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.dbiir.harp.utils.common.yaml.shortcuts.AgentYamlShortcuts;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML rule configuration shortcuts.
 */
public final class YamlRuleConfigurationShortcuts implements AgentYamlShortcuts {
    
    @SuppressWarnings("rawtypes")
    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public Map<String, Class<?>> getYamlShortcuts() {
        Collection<YamlRuleConfigurationSwapper> swappers = HarpServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class);
        Map<String, Class<?>> result = new HashMap<>(swappers.size(), 1);
        for (YamlRuleConfigurationSwapper each : swappers) {
            Class<?> yamlRuleConfigurationClass = Class.forName(((ParameterizedType) each.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].getTypeName());
            result.put(String.format("!%s", each.getRuleTagName()), yamlRuleConfigurationClass);
        }
        return result;
    }
}
