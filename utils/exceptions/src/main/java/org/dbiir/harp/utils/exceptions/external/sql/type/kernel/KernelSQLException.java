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

package org.dbiir.harp.utils.exceptions.external.sql.type.kernel;

import org.dbiir.harp.utils.exceptions.external.sql.AgentSQLException;
import org.dbiir.harp.utils.exceptions.external.sql.sqlstate.SQLState;

/**
 * Kernel SQL exception.
 */
public abstract class KernelSQLException extends AgentSQLException {
    
    private static final long serialVersionUID = -6554922589499988153L;
    
    private static final int TYPE_OFFSET = 1;
    
    public KernelSQLException(final SQLState sqlState, final int kernelCode, final int errorCode, final String reason, final Object... messageArgs) {
        super(sqlState, TYPE_OFFSET, kernelCode * 1000 + errorCode, reason, messageArgs);
    }
    
    public KernelSQLException(final SQLState sqlState, final int kernelCode, final int errorCode, final String reason, final Exception cause) {
        super(sqlState.getValue(), TYPE_OFFSET, kernelCode * 1000 + errorCode, reason, cause);
    }
}
