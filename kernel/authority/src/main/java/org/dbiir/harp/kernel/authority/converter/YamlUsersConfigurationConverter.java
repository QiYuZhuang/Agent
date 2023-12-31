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

package org.dbiir.harp.kernel.authority.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dbiir.harp.kernel.authority.yaml.config.YamlUserConfiguration;
import org.dbiir.harp.kernel.authority.yaml.swapper.YamlUserSwapper;
import org.dbiir.harp.utils.common.metadata.user.AgentUser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML Users content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlUsersConfigurationConverter {
    
    private static final YamlUserSwapper SWAPPER = new YamlUserSwapper();
    
    /**
     * Convert to Agent users.
     *
     * @param users users YAML content
     * @return Agent users
     */
    public static Collection<AgentUser> convertToAgentUser(final Collection<YamlUserConfiguration> users) {
        return users.stream().map(SWAPPER::swapToObject).collect(Collectors.toList());
    }
    
    /**
     * Convert to YAML user configurations.
     *
     * @param users users YAML content
     * @return YAML user configurations
     */
    public static Collection<YamlUserConfiguration> convertToYamlUserConfiguration(final Collection<AgentUser> users) {
        Collection<YamlUserConfiguration> result = new LinkedList<>();
        users.stream().map(SWAPPER::swapToYamlConfiguration).forEach(result::add);
        return result;
    }
}
