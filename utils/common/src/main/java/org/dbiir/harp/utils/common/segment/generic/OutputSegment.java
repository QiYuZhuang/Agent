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

package org.dbiir.harp.utils.common.segment.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dbiir.harp.utils.common.segment.SQLSegment;
import org.dbiir.harp.utils.common.segment.dml.column.ColumnSegment;
import org.dbiir.harp.utils.common.segment.dml.item.ColumnProjectionSegment;
import org.dbiir.harp.utils.common.segment.generic.table.TableNameSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Output segment.
 */
@RequiredArgsConstructor
@Getter
public final class OutputSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    @Setter
    private TableNameSegment tableName;
    
    private final Collection<ColumnProjectionSegment> outputColumns = new LinkedList<>();
    
    private final Collection<ColumnSegment> tableColumns = new LinkedList<>();
}
