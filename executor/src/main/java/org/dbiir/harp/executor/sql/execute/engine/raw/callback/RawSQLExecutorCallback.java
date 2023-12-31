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

package org.dbiir.harp.executor.sql.execute.engine.raw.callback;

import com.google.common.base.Preconditions;
import org.dbiir.harp.executor.kernel.model.ExecutorCallback;
import org.dbiir.harp.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.dbiir.harp.executor.sql.execute.result.ExecuteResult;
import org.dbiir.harp.executor.sql.process.ExecuteIDContext;
import org.dbiir.harp.executor.sql.process.ExecuteProcessEngine;
import org.dbiir.harp.utils.common.spi.HarpServiceLoader;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Raw SQL executor callback.
 */
public final class RawSQLExecutorCallback implements ExecutorCallback<RawSQLExecutionUnit, ExecuteResult> {
    
    @SuppressWarnings("rawtypes")
    private final Collection<RawExecutorCallback> callbacks;
    
    public RawSQLExecutorCallback() {
        callbacks = HarpServiceLoader.getServiceInstances(RawExecutorCallback.class);
        Preconditions.checkState(!callbacks.isEmpty(), "No raw executor callback implementation found.");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<ExecuteResult> execute(final Collection<RawSQLExecutionUnit> inputs, final boolean isTrunkThread) throws SQLException {
        Collection<ExecuteResult> result = callbacks.iterator().next().execute(inputs, isTrunkThread);
        if (!ExecuteIDContext.isEmpty()) {
            ExecuteProcessEngine executeProcessEngine = new ExecuteProcessEngine();
            for (RawSQLExecutionUnit each : inputs) {
                executeProcessEngine.finishExecution(each);
            }
        }
        return result;
    }
}
