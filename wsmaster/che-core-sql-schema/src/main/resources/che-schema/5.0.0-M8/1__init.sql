--
-- Copyright (c) 2012-2016 Codenvy, S.A.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--   Codenvy, S.A. - initial API and implementation
--

-- Account ---------------------------------------------------------------------
CREATE TABLE account (
    id          VARCHAR(255)    NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    type        VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_account_name ON account (name);
--------------------------------------------------------------------------------


-- User ------------------------------------------------------------------------
CREATE TABLE usr (
    id          VARCHAR(255)    NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    account_id  VARCHAR(255)    NOT NULL,
    password    VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_usr_email ON usr (email);
-- constraints
ALTER TABLE usr ADD CONSTRAINT fk_usr_account_id FOREIGN KEY (account_id) REFERENCES account (id);
--------------------------------------------------------------------------------


-- User aliases ----------------------------------------------------------------
CREATE TABLE user_aliases (
    user_id         VARCHAR(255),
    alias           VARCHAR(255) NOT NULL UNIQUE
);
--indexes
CREATE INDEX index_user_aliases_alias ON user_aliases (alias);
--constraints
ALTER TABLE user_aliases ADD CONSTRAINT fk_user_aliases_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- Profile ---------------------------------------------------------------------
CREATE TABLE profile (
    userid      VARCHAR(255) NOT NULL,

    PRIMARY KEY (userid)
);
-- constraints
ALTER TABLE profile ADD CONSTRAINT fk_profile_userid FOREIGN KEY (userid) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- Profile Attribute -----------------------------------------------------------
CREATE TABLE profile_attributes (
    user_id     VARCHAR(255),
    value       VARCHAR(255)    NOT NULL,
    name        VARCHAR(255)
);
-- constraints
ALTER TABLE profile_attributes ADD CONSTRAINT unq_profile_attributes_0 UNIQUE (user_id, name);
ALTER TABLE profile_attributes ADD CONSTRAINT fk_profile_attributes_user_id FOREIGN KEY (user_id) REFERENCES profile (userid);
--------------------------------------------------------------------------------


-- Preferences -----------------------------------------------------------------
CREATE TABLE preference (
    userid      VARCHAR(255)    NOT NULL,

    PRIMARY KEY (userid)
);
-- constraints
ALTER TABLE preference ADD CONSTRAINT fk_preference_userid FOREIGN KEY (userid) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- Preferences Preferences -----------------------------------------------------
CREATE TABLE preference_preferences (
    preference_userid   VARCHAR(255),
    value               TEXT,
    name                VARCHAR(255)
);
-- constraints
ALTER TABLE preference_preferences ADD CONSTRAINT fk_preference_preferences_preference_userid FOREIGN KEY (preference_userid) REFERENCES preference (userid);
--------------------------------------------------------------------------------


-- SSH -------------------------------------------------------------------------
CREATE TABLE sshkeypair (
    owner       VARCHAR(255)    NOT NULL,
    service     VARCHAR(255)    NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    privatekey  TEXT,
    publickey   TEXT,

    PRIMARY KEY (owner, service, name)
);
-- constraints
ALTER TABLE sshkeypair ADD CONSTRAINT fk_sshkeypair_owner FOREIGN KEY (owner) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- Workspace configuration -----------------------------------------------------
CREATE TABLE workspaceconfig (
    id          BIGINT          NOT NULL,
    defaultenv  VARCHAR(255)    NOT NULL,
    description TEXT,
    name        VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Workspace -------------------------------------------------------------------
CREATE TABLE workspace (
    id              VARCHAR(255)        NOT NULL,
    istemporary     BOOLEAN,
    name            VARCHAR(255),
    accountid       VARCHAR(255)        NOT NULL,
    config_id       BIGINT,

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE workspace ADD CONSTRAINT unq_workspace_0 UNIQUE (name, accountid);
ALTER TABLE workspace ADD CONSTRAINT fx_workspace_accountid FOREIGN KEY (accountid) REFERENCES account (id);
ALTER TABLE workspace ADD CONSTRAINT fk_workspace_config_id FOREIGN KEY (config_id) REFERENCES workspaceconfig (id);
--------------------------------------------------------------------------------


--Workspace attributes ---------------------------------------------------------
CREATE TABLE workspace_attributes (
    workspace_id    VARCHAR(255),
    attributes      VARCHAR(255),
    attributes_key  VARCHAR(255)
);
--constraints
ALTER TABLE workspace_attributes ADD CONSTRAINT fk_workspace_attributes_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);
--------------------------------------------------------------------------------


-- Project source --------------------------------------------------------------
CREATE TABLE sourcestorage (
    id          BIGINT      NOT NULL,
    location    TEXT,
    type        VARCHAR(255),

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Project source parameters----------------------------------------------------
CREATE TABLE sourcestorage_parameters (
    sourcestorage_id        BIGINT,
    parameters              VARCHAR(255),
    parameters_key          VARCHAR(255)
);
--constraints
ALTER TABLE sourcestorage_parameters ADD CONSTRAINT fk_sourcestorage_parameters_sourcestorage_id FOREIGN KEY (sourcestorage_id) REFERENCES sourcestorage (id);
--------------------------------------------------------------------------------


-- Project configuration -------------------------------------------------------
CREATE TABLE projectconfig (
    id              BIGINT          NOT NULL,
    description     TEXT,
    name            VARCHAR(255),
    path            VARCHAR(255)    NOT NULL,
    type            VARCHAR(255),
    source_id       BIGINT,
    projects_id     BIGINT,

    PRIMARY KEY (id)
);

-- constraints
ALTER TABLE projectconfig ADD CONSTRAINT fk_projectconfig_projects_id FOREIGN KEY (projects_id) REFERENCES workspaceconfig (id);
ALTER TABLE projectconfig ADD CONSTRAINT fk_projectconfig_source_id FOREIGN KEY (source_id) REFERENCES sourcestorage (id);
--------------------------------------------------------------------------------


-- Project attributes ----------------------------------------------------------
CREATE TABLE projectattribute (
    id              BIGINT          NOT NULL,
    name            VARCHAR(255),
    dbattributes_id BIGINT,

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE projectattribute ADD CONSTRAINT fk_projectattribute_dbattributes_id FOREIGN KEY (dbattributes_id) REFERENCES projectconfig (id);
--------------------------------------------------------------------------------


-- Project attribute values ----------------------------------------------------
CREATE TABLE projectattribute_values (
    projectattribute_id     BIGINT,
    values                  VARCHAR(255)
);
--constraints
ALTER TABLE projectattribute_values ADD CONSTRAINT fk_projectattribute_values_projectattribute_id FOREIGN KEY (projectattribute_id) REFERENCES projectattribute (id);
--------------------------------------------------------------------------------


-- Project mixins --------------------------------------------------------------
CREATE TABLE projectconfig_mixins (
    projectconfig_id    BIGINT,
    mixins              VARCHAR(255)
);
--constraints
ALTER TABLE projectconfig_mixins ADD CONSTRAINT fk_projectconfig_mixins_projectconfig_id FOREIGN KEY (projectconfig_id) REFERENCES projectconfig (id);
--------------------------------------------------------------------------------


-- Commands --------------------------------------------------------------------
CREATE TABLE command (
    id              BIGINT      NOT NULL,
    commandline     TEXT,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(255) NOT NULL,
    commands_id     BIGINT,

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE command ADD CONSTRAINT fk_command_commands_id FOREIGN KEY (commands_id) REFERENCES workspaceconfig (id);
--------------------------------------------------------------------------------


-- Command attributes ----------------------------------------------------------
CREATE TABLE command_attributes (
    command_id  BIGINT,
    value       TEXT,
    name        VARCHAR(255)
);
--constraints
ALTER TABLE command_attributes ADD CONSTRAINT fk_command_attributes_command_id FOREIGN KEY (command_id) REFERENCES command (id);
--------------------------------------------------------------------------------


-- Workspace Environments ------------------------------------------------------
CREATE TABLE environment (
    id                  BIGINT          NOT NULL,
    content             TEXT,
    contenttype         VARCHAR(255),
    location            TEXT,
    type                VARCHAR(255),
    environments_id     BIGINT,
    environments_key    VARCHAR(255),

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE environment ADD CONSTRAINT fk_environment_environments_id FOREIGN KEY (environments_id) REFERENCES workspaceconfig (id);
--------------------------------------------------------------------------------


-- Environment machines --------------------------------------------------------
CREATE TABLE externalmachine (
    id              BIGINT          NOT NULL,
    machines_id     BIGINT,
    machines_key    VARCHAR(255),

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE externalmachine ADD CONSTRAINT fk_externalmachine_machines_id FOREIGN KEY (machines_id) REFERENCES environment (id);
--------------------------------------------------------------------------------


-- Environment machine agents  -------------------------------------------------
CREATE TABLE externalmachine_agents (
    externalmachine_id  BIGINT,
    agents              VARCHAR(255)
);
-- constraints
ALTER TABLE externalmachine_agents ADD CONSTRAINT fk_externalmachine_agents_externalmachine_id FOREIGN KEY (externalmachine_id) REFERENCES externalmachine (id);
--------------------------------------------------------------------------------


-- Environment machine attributes ----------------------------------------------
CREATE TABLE externalmachine_attributes (
    externalmachine_id      BIGINT,
    attributes              VARCHAR(255),
    attributes_key          VARCHAR(255)
);
--constraints
ALTER TABLE externalmachine_attributes ADD CONSTRAINT fk_externalmachine_attributes_externalmachine_id FOREIGN KEY (externalmachine_id) REFERENCES externalmachine (id);
--------------------------------------------------------------------------------


-- Machine servers configuration -----------------------------------------------
CREATE TABLE serverconf (
    id              BIGINT          NOT NULL,
    port            VARCHAR(255),
    protocol        VARCHAR(255),
    servers_id      BIGINT,
    servers_key     VARCHAR(255),

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE serverconf ADD CONSTRAINT fk_serverconf_servers_id FOREIGN KEY (servers_id) REFERENCES externalmachine (id);
--------------------------------------------------------------------------------

-- Machine server properties ---------------------------------------------------
CREATE TABLE serverconf_properties (
    serverconf_id       BIGINT,
    properties          VARCHAR(255),
    properties_key      VARCHAR(255)
);
--constraints
ALTER TABLE serverconf_properties ADD CONSTRAINT fk_serverconf_properties_serverconf_id FOREIGN KEY (serverconf_id) REFERENCES serverconf (id);
--------------------------------------------------------------------------------


-- Workspace snapshots ---------------------------------------------------------
CREATE TABLE snapshot (
    id              VARCHAR(255)        NOT NULL,
    creationdate    BIGINT,
    description     TEXT,
    envname         VARCHAR(255)        NOT NULL,
    isdev           BOOLEAN,
    machinename     VARCHAR(255)        NOT NULL,
    type            VARCHAR(255),
    workspaceid     VARCHAR(255)        NOT NULL,
    content         TEXT,
    location        VARCHAR(255),
    source_type     VARCHAR(255),

    PRIMARY KEY (id)
);
--indexes
CREATE UNIQUE INDEX index_snapshot_workspaceid_envname_machinename ON snapshot (workspaceid, envname, machinename);
--constraints
ALTER TABLE snapshot ADD CONSTRAINT fk_snapshot_workspaceid FOREIGN KEY (workspaceid) REFERENCES workspace (id);
--------------------------------------------------------------------------------


-- Recipe ----------------------------------------------------------------------
CREATE TABLE recipe (
    id              VARCHAR(255) NOT NULL,
    creator         VARCHAR(255),
    description     TEXT,
    name            VARCHAR(255),
    script          TEXT,
    type            VARCHAR(255),

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Recipe tags -----------------------------------------------------------------
CREATE TABLE recipe_tags (
    recipe_id   VARCHAR(255),
    tag         VARCHAR(255)
);
--indexes
CREATE INDEX index_recipe_tags_tag ON recipe_tags (tag);
--constraints
ALTER TABLE recipe_tags ADD CONSTRAINT fs_recipe_tags_recipe_id FOREIGN KEY (recipe_id) REFERENCES recipe (id);
--------------------------------------------------------------------------------


-- Stack -----------------------------------------------------------------------
CREATE TABLE stack (
    id                  VARCHAR(255)        NOT NULL,
    creator             VARCHAR(255),
    description         TEXT,
    name                VARCHAR(255)        NOT NULL    UNIQUE,
    scope               VARCHAR(255),
    origin              VARCHAR(255),
    type                VARCHAR(255),
    data                BYTEA,
    mediatype           VARCHAR(255),
    icon_name           VARCHAR(255),
    workspaceconfig_id  BIGINT,

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE stack ADD CONSTRAINT fk_stack_workspaceconfig_id FOREIGN KEY (workspaceconfig_id) REFERENCES workspaceconfig (id);
--------------------------------------------------------------------------------


-- Stack components ------------------------------------------------------------
CREATE TABLE stack_components (
    name        VARCHAR(255),
    version     VARCHAR(255),
    stack_id    VARCHAR(255)
);
--constraints
ALTER TABLE stack_components ADD CONSTRAINT fk_stack_components_stack_id FOREIGN KEY (stack_id) REFERENCES stack (id);
--------------------------------------------------------------------------------


-- Stack tags ------------------------------------------------------------------
CREATE TABLE stack_tags (
    stack_id    VARCHAR(255),
    tag         VARCHAR(255)
);
--indexes
CREATE INDEX index_stack_tags_tag ON stack_tags (tag);
--constraints
ALTER TABLE stack_tags ADD CONSTRAINT fk_stack_tags_stack_id FOREIGN KEY (stack_id) REFERENCES stack (id);
--------------------------------------------------------------------------------

-- Sequence table --------------------------------------------------------------
CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT NUMERIC(38), PRIMARY KEY (SEQ_NAME));
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SEQ_GEN', 0);
--------------------------------------------------------------------------------
