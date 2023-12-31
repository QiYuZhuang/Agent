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

package org.dbiir.harp.kernel.core.exception;


import org.dbiir.harp.kernel.transaction.api.TransactionType;
import org.dbiir.harp.utils.exceptions.external.sql.sqlstate.XOpenSQLState;

/**
 * Transaction manager not existed exception.
 */
public final class TransactionManagerNotExistedException extends TransactionSQLException {
    
    private static final long serialVersionUID = 3831707403758598143L;
    
    public TransactionManagerNotExistedException(final TransactionType transactionType) {
        super(XOpenSQLState.INVALID_TRANSACTION_STATE, 1, String.format("Can not find transaction manager of `%s`", transactionType));
    }
}
