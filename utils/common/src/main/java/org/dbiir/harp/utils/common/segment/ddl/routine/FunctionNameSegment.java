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

package org.dbiir.harp.utils.common.segment.ddl.routine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dbiir.harp.utils.common.segment.SQLSegment;
import org.dbiir.harp.utils.common.segment.generic.OwnerAvailable;
import org.dbiir.harp.utils.common.segment.generic.OwnerSegment;
import org.dbiir.harp.utils.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Function name segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class FunctionNameSegment implements SQLSegment, OwnerAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private OwnerSegment owner;
    
    @Override
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
}
