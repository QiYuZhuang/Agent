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

package org.dbiir.harp.parser.type.mysql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.dbiir.harp.parser.api.visitor.operation.SQLStatementVisitor;
import org.dbiir.harp.parser.api.visitor.type.DCLSQLVisitor;
import org.dbiir.harp.parser.type.mysql.autogen.MySQLStatementParser.*;
import org.dbiir.harp.utils.common.ASTNode;
import org.dbiir.harp.utils.common.segment.generic.ACLTypeEnum;
import org.dbiir.harp.utils.common.segment.generic.GrantLevelSegment;
import org.dbiir.harp.utils.common.segment.generic.PrivilegeTypeEnum;
import org.dbiir.harp.utils.common.statement.mysql.dcl.*;
import org.dbiir.harp.utils.common.statement.mysql.segment.*;
import org.dbiir.harp.utils.common.value.identifier.IdentifierValue;
import org.dbiir.harp.utils.common.value.literal.impl.NumberLiteralValue;
import org.dbiir.harp.utils.common.value.literal.impl.StringLiteralValue;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * DCL Statement SQL visitor for MySQL.
 */
@NoArgsConstructor
public final class MySQLDCLStatementSQLVisitor extends MySQLStatementSQLVisitor implements DCLSQLVisitor, SQLStatementVisitor {
    
    public MySQLDCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeTo(final GrantRoleOrPrivilegeToContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeOnTo(final GrantRoleOrPrivilegeOnToContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        if (null == ctx.roleOrPrivileges()) {
            result.setAllPrivileges(true);
        } else {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        }
        result.setLevel(generateGrantLevel(ctx.grantIdentifier()));
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        if (null != ctx.aclType()) {
            switch (ctx.aclType().getText().toLowerCase()) {
                case "function":
                    result.setAclType(ACLTypeEnum.FUNCTION);
                    break;
                case "procedure":
                    result.setAclType(ACLTypeEnum.PROCEDURE);
                    break;
                default:
                    result.setAclType(ACLTypeEnum.TABLE);
                    break;
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantProxy(final GrantProxyContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.PROXY().getSymbol().getStartIndex(), ctx.PROXY().getSymbol().getStopIndex(), PrivilegeTypeEnum.GRANT);
        result.getRoleOrPrivileges().add(new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    private void fillRoleOrPrivileges(final MySQLGrantStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((MySQLRoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private void fillRoleOrPrivileges(final MySQLRevokeStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((MySQLRoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private GrantLevelSegment generateGrantLevel(final GrantIdentifierContext ctx) {
        if (ctx instanceof GrantLevelGlobalContext) {
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "*", "*");
            
        }
        if (ctx instanceof GrantLevelSchemaGlobalContext) {
            String schemaName = new IdentifierValue(((GrantLevelSchemaGlobalContext) ctx).schemaName().getText()).getValue();
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), schemaName, "*");
        }
        String schemaName = null;
        String tableName;
        if (null != ((GrantLevelTableContext) ctx).tableName().owner()) {
            schemaName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().owner().getText()).getValue();
        }
        tableName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().name().getText()).getValue();
        return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), schemaName, tableName);
    }
    
    @Override
    public ASTNode visitRoleOrDynamicPrivilege(final RoleOrDynamicPrivilegeContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, null, null);
    }
    
    @Override
    public ASTNode visitRoleAtHost(final RoleAtHostContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        String host = new IdentifierValue(ctx.textOrIdentifier().getText()).getValue();
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, host, null);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSelect(final StaticPrivilegeSelectContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SELECT);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeInsert(final StaticPrivilegeInsertContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.INSERT);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUpdate(final StaticPrivilegeUpdateContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.UPDATE);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReferences(final StaticPrivilegeReferencesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.REFERENCES);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDelete(final StaticPrivilegeDeleteContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DELETE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUsage(final StaticPrivilegeUsageContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.USAGE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeIndex(final StaticPrivilegeIndexContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.INDEX);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlter(final StaticPrivilegeAlterContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.ALTER);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreate(final StaticPrivilegeCreateContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDrop(final StaticPrivilegeDropContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DROP);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeExecute(final StaticPrivilegeExecuteContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.EXECUTE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReload(final StaticPrivilegeReloadContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.RELOAD);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShutdown(final StaticPrivilegeShutdownContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SHUTDOWN);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeProcess(final StaticPrivilegeProcessContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.PROCESS);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeFile(final StaticPrivilegeFileContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.FILE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeGrant(final StaticPrivilegeGrantContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.GRANT);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowDatabases(final StaticPrivilegeShowDatabasesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SHOW_DB);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSuper(final StaticPrivilegeSuperContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SUPER);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTemporaryTables(final StaticPrivilegeCreateTemporaryTablesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_TMP);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeLockTables(final StaticPrivilegeLockTablesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.LOCK_TABLES);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationSlave(final StaticPrivilegeReplicationSlaveContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.REPL_SLAVE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationClient(final StaticPrivilegeReplicationClientContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.REPL_CLIENT);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateView(final StaticPrivilegeCreateViewContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_VIEW);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowView(final StaticPrivilegeShowViewContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SHOW_VIEW);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRoutine(final StaticPrivilegeCreateRoutineContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_PROC);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlterRoutine(final StaticPrivilegeAlterRoutineContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.ALTER_PROC);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateUser(final StaticPrivilegeCreateUserContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_USER);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeEvent(final StaticPrivilegeEventContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.EVENT);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeTrigger(final StaticPrivilegeTriggerContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.TRIGGER);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTablespace(final StaticPrivilegeCreateTablespaceContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_TABLESPACE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRole(final StaticPrivilegeCreateRoleContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_ROLE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDropRole(final StaticPrivilegeDropRoleContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DROP_ROLE);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitRevokeFrom(final RevokeFromContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement();
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        }
        for (UsernameContext each : ctx.userList().username()) {
            result.getFromUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevokeOnFrom(final RevokeOnFromContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement();
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        } else if (null != ctx.PROXY()) {
            MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DROP_ROLE);
            result.getRoleOrPrivileges().add(new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
            result.setOnUser((UserSegment) visit(ctx.username()));
        }
        if (null != ctx.grantIdentifier()) {
            result.setLevel(generateGrantLevel(ctx.grantIdentifier()));
        }
        for (UsernameContext each : ctx.userList().username()) {
            result.getFromUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        MySQLCreateUserStatement result = new MySQLCreateUserStatement();
        for (CreateUserEntryContext each : ctx.createUserList().createUserEntry()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        if (null != ctx.defaultRoleClause()) {
            for (RoleNameContext each : ctx.defaultRoleClause().roleName()) {
                result.getDefaultRoles().add(each.getText());
            }
        }
        if (null != ctx.requireClause()) {
            result.setTlsOptionSegment((TLSOptionSegment) visit(ctx.requireClause()));
        }
        if (null != ctx.connectOptions()) {
            result.setUserResource((UserResourceSegment) visit(ctx.connectOptions()));
        }
        if (null != ctx.accountLockPasswordExpireOptions()) {
            result.setPasswordOrLockOption((PasswordOrLockOptionSegment) visit(ctx.accountLockPasswordExpireOptions()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRequireClause(final RequireClauseContext ctx) {
        TLSOptionSegment result = new TLSOptionSegment();
        if (null != ctx.NONE()) {
            result.setType(SSLTypeEnum.SSL_TYPE_NONE);
        } else if (null != ctx.X509()) {
            result.setType(SSLTypeEnum.SSL_TYPE_X509);
        } else if (null != ctx.SSL()) {
            result.setType(SSLTypeEnum.SSL_TYPE_ANY);
        } else {
            result.setType(SSLTypeEnum.SSL_TYPE_SPECIFIED);
            for (TlsOptionContext each : ctx.tlsOption()) {
                if (null != each.SUBJECT()) {
                    result.setX509Subject(each.string_().getText());
                } else if (null != each.ISSUER()) {
                    result.setX509Issuer(each.string_().getText());
                } else if (null != each.CIPHER()) {
                    result.setX509Cipher(each.string_().getText());
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitConnectOptions(final ConnectOptionsContext ctx) {
        UserResourceSegment result = new UserResourceSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        for (ConnectOptionContext each : ctx.connectOption()) {
            if (null != each.MAX_QUERIES_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.QUERIES_PER_HOUR);
                result.setQuestions(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_UPDATES_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.UPDATES_PER_HOUR);
                result.setUpdates(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_CONNECTIONS_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.CONNECTIONS_PER_HOUR);
                result.setConnPerHour(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_USER_CONNECTIONS()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.USER_CONNECTIONS);
                result.setUserConn(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryNoOption(final CreateUserEntryNoOptionContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.username());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryIdentifiedBy(final CreateUserEntryIdentifiedByContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.username());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.string_()) {
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
        } else {
            result.setHasPasswordGenerator(true);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setUsesIdentifiedWithClause(false);
        }
        result.setRetainCurrentPassword(false);
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryIdentifiedWith(final CreateUserEntryIdentifiedWithContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.username());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.textOrIdentifier()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else if (null != ctx.AS()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else if (null != ctx.BY() && null != ctx.string_()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setUsesIdentifiedWithClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(true);
            result.setUsesIdentifiedByClause(true);
            result.setUsesIdentifiedWithClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        }
        return result;
    }
    
    @Override
    public ASTNode visitAccountLockPasswordExpireOptions(final AccountLockPasswordExpireOptionsContext ctx) {
        PasswordOrLockOptionSegment result = new PasswordOrLockOptionSegment();
        for (AccountLockPasswordExpireOptionContext each : ctx.accountLockPasswordExpireOption()) {
            fillAccountLockPasswordExpireOption(result, each);
        }
        return result;
    }
    
    private void fillAccountLockPasswordExpireOption(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.ACCOUNT()) {
            fillAccountLock(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.EXPIRE()) {
            fillPasswordExpire(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.HISTORY()) {
            fillPasswordHistory(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.REUSE()) {
            fillPasswordReuse(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.REQUIRE()) {
            fillPasswordRequire(segment, ctx);
        } else if (null != ctx.FAILED_LOGIN_ATTEMPTS()) {
            segment.setUpdateFailedLoginAttempts(true);
            segment.setFailedLoginAttempts(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
        } else {
            if (null == ctx.UNBOUNDED()) {
                segment.setUpdatePasswordLockTime(true);
                segment.setPasswordLockTime(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            } else {
                segment.setUpdatePasswordLockTime(true);
                segment.setPasswordLockTime(-1);
            }
        }
    }
    
    private void fillAccountLock(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null == ctx.LOCK()) {
            segment.setUpdateAccountLockedColumn(true);
            segment.setAccountLocked(false);
        } else {
            segment.setUpdateAccountLockedColumn(true);
            segment.setAccountLocked(true);
        }
    }
    
    private void fillPasswordExpire(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.INTERVAL()) {
            segment.setExpireAfterDays(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordExpiredColumn(false);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(false);
        } else if (null != ctx.NEVER()) {
            segment.setExpireAfterDays(0);
            segment.setUpdatePasswordExpiredColumn(false);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(false);
        } else if (null != ctx.DEFAULT()) {
            segment.setExpireAfterDays(0);
            segment.setUpdatePasswordExpiredColumn(false);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(true);
        } else {
            segment.setExpireAfterDays(0);
            segment.setUpdatePasswordExpiredColumn(true);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(true);
        }
    }
    
    private void fillPasswordHistory(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null == ctx.DEFAULT()) {
            segment.setPasswordHistoryLength(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordHistory(true);
            segment.setUseDefaultPasswordHistory(false);
        } else {
            segment.setPasswordHistoryLength(0);
            segment.setUpdatePasswordHistory(true);
            segment.setUseDefaultPasswordHistory(true);
        }
    }
    
    private void fillPasswordReuse(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null == ctx.DEFAULT()) {
            segment.setPasswordReuseInterval(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordReuseInterval(true);
            segment.setUseDefaultPasswordReuseInterval(false);
        } else {
            segment.setPasswordReuseInterval(0);
            segment.setUpdatePasswordReuseInterval(true);
            segment.setUseDefaultPasswordReuseInterval(true);
        }
    }
    
    private void fillPasswordRequire(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.DEFAULT()) {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeEnum.DEFAULT);
        } else if (null != ctx.OPTIONAL()) {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeEnum.NO);
        } else {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeEnum.YES);
        }
    }
    
    @Override
    public ASTNode visitUsername(final UsernameContext ctx) {
        UserSegment result = new UserSegment();
        if (null != ctx.userIdentifierOrText()) {
            result.setUser(new IdentifierValue(ctx.userIdentifierOrText().textOrIdentifier(0).getText()).getValue());
            if (null != ctx.userIdentifierOrText().AT_()) {
                result.setHost(new IdentifierValue(ctx.userIdentifierOrText().textOrIdentifier(1).getText()).getValue());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        MySQLDropUserStatement result = new MySQLDropUserStatement();
        result.getUsers().addAll(ctx.username().stream().map(UsernameContext::getText).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new MySQLAlterUserStatement();
    }
    
    @Override
    public ASTNode visitRenameUser(final RenameUserContext ctx) {
        return new MySQLRenameUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new MySQLCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new MySQLDropRoleStatement();
    }
    
    @Override
    public ASTNode visitSetDefaultRole(final SetDefaultRoleContext ctx) {
        return new MySQLSetDefaultRoleStatement();
    }
    
    @Override
    public ASTNode visitSetRole(final SetRoleContext ctx) {
        return new MySQLSetRoleStatement();
    }
    
    @Override
    public ASTNode visitSetPassword(final SetPasswordContext ctx) {
        return new MySQLSetPasswordStatement();
    }
}
