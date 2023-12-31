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

package org.dbiir.harp.db.protocol.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dbiir.harp.db.protocol.payload.MySQLPacketPayload;

/**
 * Command packet type loader for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCommandPacketTypeLoader {
    
    /**
     * Get command packet type.
     *
     * @param payload packet payload for MySQL
     * @return command packet type for MySQL
     */
    public static MySQLCommandPacketType getCommandPacketType(final MySQLPacketPayload payload) {
        return MySQLCommandPacketType.valueOf(payload.readInt1());
    }
}
