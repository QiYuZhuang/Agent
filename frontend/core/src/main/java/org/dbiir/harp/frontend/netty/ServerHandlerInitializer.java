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

package org.dbiir.harp.frontend.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dbiir.harp.db.protocol.codec.PacketCodec;
import org.dbiir.harp.db.protocol.netty.ChannelAttrInitializer;
import org.dbiir.harp.db.protocol.netty.ProxyFlowControlHandler;
import org.dbiir.harp.utils.common.database.type.DatabaseType;
import org.dbiir.harp.utils.common.spi.type.typed.TypedSPILoader;
import org.dbiir.harp.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Server handler initializer.
 */
@RequiredArgsConstructor
@Slf4j
public final class ServerHandlerInitializer extends ChannelInitializer<Channel> {
    
    private final DatabaseType databaseType;
    
    @Override
    protected void initChannel(final Channel socketChannel) {
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = TypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType.getType());
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ChannelAttrInitializer());
        pipeline.addLast(new PacketCodec(databaseProtocolFrontendEngine.getCodecEngine()));
        pipeline.addLast(new FrontendChannelLimitationInboundHandler(databaseProtocolFrontendEngine));
        pipeline.addLast(ProxyFlowControlHandler.class.getSimpleName(), new ProxyFlowControlHandler());
        pipeline.addLast(FrontendChannelInboundHandler.class.getSimpleName(), new FrontendChannelInboundHandler(databaseProtocolFrontendEngine, socketChannel));
        databaseProtocolFrontendEngine.initChannel(socketChannel);
    }
}
