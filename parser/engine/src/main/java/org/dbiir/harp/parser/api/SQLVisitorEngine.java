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

package org.dbiir.harp.parser.api;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.dbiir.harp.parser.core.ParseASTNode;
import org.dbiir.harp.parser.core.database.visitor.SQLVisitorFactory;
import org.dbiir.harp.parser.core.database.visitor.SQLVisitorRule;
import org.dbiir.harp.utils.common.segment.generic.CommentSegment;
import org.dbiir.harp.utils.common.statement.AbstractSQLStatement;

import java.util.Properties;

/**
 * SQL visitor engine.
 */
@RequiredArgsConstructor
public final class SQLVisitorEngine {
    
    private final String databaseType;
    
    private final String visitorType;
    
    private final boolean isParseComment;
    
    private final Properties props;
    
    /**
     * Visit parse context.
     *
     * @param parseASTNode parse AST node
     * @param <T> type of SQL visitor result
     * @return SQL visitor result
     */
    public <T> T visit(final ParseASTNode parseASTNode) {
        ParseTreeVisitor<T> visitor = SQLVisitorFactory.newInstance(databaseType, visitorType, SQLVisitorRule.valueOf(parseASTNode.getRootNode().getClass()), props);
        T result = parseASTNode.getRootNode().accept(visitor);
        if (isParseComment) {
            appendSQLComments(parseASTNode, result);
        }
        return result;
    }
    
    private <T> void appendSQLComments(final ParseASTNode parseASTNode, final T visitResult) {
        if (visitResult instanceof AbstractSQLStatement) {
            for (Token each : parseASTNode.getHiddenTokens()) {
                ((AbstractSQLStatement) visitResult).getCommentSegments().add(new CommentSegment(each.getText(), each.getStartIndex(), each.getStopIndex()));
            }
        }
    }
}
