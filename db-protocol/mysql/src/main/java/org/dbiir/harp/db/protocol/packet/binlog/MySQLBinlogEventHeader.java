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

package org.dbiir.harp.db.protocol.packet.binlog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.dbiir.harp.db.protocol.packet.MySQLPacket;
import org.dbiir.harp.db.protocol.payload.MySQLPacketPayload;

/**
 * MySQL binlog event header.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/binlog-event-header.html">Binlog Event header</a>
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class MySQLBinlogEventHeader implements MySQLPacket {
    
    /**
     * MySQL binlog event header length is 19 in binlog version 3 and 4.
     */
    public static final int MYSQL_BINLOG_EVENT_HEADER_LENGTH = 19;
    
    private final int timestamp;
    
    private final int eventType;
    
    private final int serverId;
    
    /**
     * Size of the event (header, post-header, body).
     */
    private final int eventSize;
    
    private final int logPos;
    
    private final int flags;
    
    private final int checksumLength;
    
    public MySQLBinlogEventHeader(final MySQLPacketPayload payload, final int checksumLength) {
        timestamp = payload.readInt4();
        eventType = payload.readInt1();
        serverId = payload.readInt4();
        eventSize = payload.readInt4();
        logPos = payload.readInt4();
        flags = payload.readInt2();
        this.checksumLength = checksumLength;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt4(timestamp);
        payload.writeInt1(eventType);
        payload.writeInt4(serverId);
        payload.writeInt4(eventSize);
        payload.writeInt4(logPos);
        payload.writeInt2(flags);
    }
}
