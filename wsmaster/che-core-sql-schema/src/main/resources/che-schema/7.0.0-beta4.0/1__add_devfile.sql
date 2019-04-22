--
-- Copyright (c) 2012-2019 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

-- add devfile table
CREATE TABLE devfile (
    id            BIGINT          NOT NULL,
    spec_version  VARCHAR(255)    NOT NULL,
    name          VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id)
);

-- devfile attributes
CREATE TABLE devfile_attributes (
    devfile_id     BIGINT,
    value          TEXT,
    name           VARCHAR(255)
);
-- constraints & indexes
ALTER TABLE devfile_attributes ADD CONSTRAINT fk_devfile_attributes_devfile_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
CREATE UNIQUE INDEX index_devfile_attributes_names ON devfile_attributes (devfile_id, name);

-----------------------------------------------------------------------------------------------------------------------------------

-- devfile project
CREATE TABLE devfile_project (
    id                   BIGINT       NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    clone_path           TEXT,
    type                 VARCHAR(255) NOT NULL,
    location             TEXT,
    branch               VARCHAR(255),
    start_point          VARCHAR(255),
    tag                  VARCHAR(255),
    commit_id            VARCHAR(255),
    devfile_id           BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_project ADD CONSTRAINT fk_devfile_projects_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
CREATE UNIQUE INDEX index_devfile_project_name ON devfile_project (devfile_id, name);

-----------------------------------------------------------------------------------------------------------------------------------

-- devfile command
CREATE TABLE devfile_command (
    id                  BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    devfile_id          BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_command ADD CONSTRAINT fk_devfile_command_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
CREATE UNIQUE INDEX index_devfile_command_name ON devfile_command (devfile_id, name);

-- devfile command attributes
CREATE TABLE devfile_command_attributes (
    devfile_command_id     BIGINT,
    value                  TEXT,
    name                   VARCHAR(255)
);
-- constraints & indexes
ALTER TABLE devfile_command_attributes ADD CONSTRAINT fk_devfile_command_attributes_command_id FOREIGN KEY (devfile_command_id) REFERENCES devfile_command (id);
CREATE UNIQUE INDEX index_devfile_command_attributes_name ON devfile_command_attributes (devfile_command_id, name);

-- devfile command action
CREATE TABLE devfile_action (
    id                      BIGINT       NOT NULL,
    type                    VARCHAR(255) NOT NULL,
    component               VARCHAR(255) NOT NULL,
    command                 TEXT NOT NULL,
    workdir                 TEXT,
    devfile_command_id      BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_action ADD CONSTRAINT fk_devfile_actions_id FOREIGN KEY (devfile_command_id) REFERENCES devfile_command (id);
CREATE INDEX index_action_command_id ON devfile_action (devfile_command_id);


-----------------------------------------------------------------------------------------------------------------------------------

-- devfile component
CREATE TABLE devfile_component (
    id                     BIGINT       NOT NULL,
    type                   VARCHAR(255) NOT NULL,
    alias                  VARCHAR(255),
    component_id           TEXT,
    reference              TEXT,
    reference_content      TEXT,
    image                  TEXT,
    memory_limit           VARCHAR(255),
    mount_sources          BOOLEAN,
    devfile_id             BIGINT,

    PRIMARY KEY (id)
);

-- constraints & indexes
ALTER TABLE devfile_component ADD CONSTRAINT fk_devfile_component_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);


-- component command
CREATE TABLE devfile_component_command (
    devfile_component_id    BIGINT,
    command                 TEXT NOT NULL
);

-- constraints & indexes
ALTER TABLE devfile_component_command ADD CONSTRAINT fk_component_command_component_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE INDEX index_command_component_id ON devfile_component_command (devfile_component_id);

-- component arg
CREATE TABLE devfile_component_arg (
    devfile_component_id    BIGINT,
    args                    TEXT
);

--constraints
ALTER TABLE devfile_component_arg ADD CONSTRAINT fk_component_command_arg_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE INDEX index_args_component_id ON devfile_component_arg (devfile_component_id);

-- component selector
CREATE TABLE devfile_component_selector (
    devfile_component_id    BIGINT,
    selector_key            VARCHAR(255),
    selector                VARCHAR(255)
);

-- constraints & indexes
ALTER TABLE devfile_component_selector ADD CONSTRAINT fk_component_selector_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE UNIQUE INDEX index_devfile_component_selector ON devfile_component_selector (devfile_component_id, selector_key);

-- devfile endpoint
CREATE TABLE devfile_endpoint (
    id                      BIGINT       NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    port                    INTEGER      NOT NULL,
    devfile_component_id    BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_endpoint ADD CONSTRAINT fk_devfile_endpoint_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE UNIQUE INDEX index_devfile_endpoint_component_name ON devfile_endpoint (devfile_component_id, name);

-- devfile endpoint attributes
CREATE TABLE devfile_endpoint_attributes (
    devfile_endpoint_id    BIGINT,
    value                  TEXT,
    name                   VARCHAR(255)
);
-- constraints & indexes
ALTER TABLE devfile_endpoint_attributes ADD CONSTRAINT fk_devfile_endpoint_attributes_id FOREIGN KEY (devfile_endpoint_id) REFERENCES devfile_endpoint (id);
CREATE UNIQUE INDEX index_devfile_endpoint_attributes_name ON devfile_endpoint_attributes (devfile_endpoint_id, name);


-- devfile component env
CREATE TABLE devfile_env (
    id                    BIGINT       NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    value                 TEXT,
    devfile_component_id  BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_env ADD CONSTRAINT fk_devfile_env_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE UNIQUE INDEX index_devfile_env_component_name ON devfile_env (devfile_component_id, name);


-- devfile component volume
CREATE TABLE devfile_volume (
    id                     BIGINT       NOT NULL,
    name                   VARCHAR(255) NOT NULL,
    container_path         TEXT,
    devfile_component_id   BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_volume ADD CONSTRAINT fk_devfile_volumes_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE UNIQUE INDEX index_devfile_volume_component_name ON devfile_volume (devfile_component_id, name);

-- devfile component entrypoint
CREATE TABLE devfile_entrypoint (
    id                         BIGINT       NOT NULL,
    parent_name                VARCHAR(255) NOT NULL,
    container_name             VARCHAR(255) NOT NULL,
    devfile_component_id       BIGINT,

    PRIMARY KEY (id)
);
-- constraints & indexes
ALTER TABLE devfile_entrypoint ADD CONSTRAINT fk_devfile_entrypoints_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE UNIQUE INDEX index_devfile_entrypoint_id_parent_container ON devfile_entrypoint (devfile_component_id, parent_name, container_name);


-- entrypoint arg
CREATE TABLE devfile_entrypoint_arg (
    devfile_entrypoint_id    BIGINT,
    arg                      VARCHAR(255) NOT NULL
);

-- constraints & indexes
ALTER TABLE devfile_entrypoint_arg ADD CONSTRAINT fk_entrypoint_arg_id FOREIGN KEY (devfile_entrypoint_id) REFERENCES devfile_entrypoint (id);
CREATE INDEX index_entrypoint_arg_entrypoint_id ON devfile_entrypoint_arg (devfile_entrypoint_id);


-- entrypoint commands
CREATE TABLE devfile_entrypoint_commands (
    devfile_entrypoint_id    BIGINT,
    command                  VARCHAR(255) NOT NULL
);

-- constraints & indexes
ALTER TABLE devfile_entrypoint_commands ADD CONSTRAINT fk_entrypoint_commands_id FOREIGN KEY (devfile_entrypoint_id) REFERENCES devfile_entrypoint (id);
CREATE INDEX index_entrypoint_commands_entrypoint_id ON devfile_entrypoint_commands (devfile_entrypoint_id);


CREATE TABLE devfile_entrypoint_selector (
    devfile_entrypoint_id    BIGINT,
    selector_key             VARCHAR(255) NOT NULL,
    selector                 VARCHAR(255) NOT NULL
);

-- constraints & indexes
ALTER TABLE devfile_entrypoint_selector ADD CONSTRAINT fk_entrypoint_selector_id FOREIGN KEY (devfile_entrypoint_id) REFERENCES devfile_entrypoint (id);
CREATE UNIQUE INDEX index_entrypoint_selectors_keys ON devfile_entrypoint_selector (devfile_entrypoint_id, selector_key);


-----------------------------------------------------------------------------------------------------------------------------------

-- add devfile into workspace
ALTER TABLE workspace ADD COLUMN devfile_id BIGINT;
