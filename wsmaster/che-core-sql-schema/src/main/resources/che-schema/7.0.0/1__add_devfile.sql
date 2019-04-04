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
    devfile_id     VARCHAR(255),
    value          TEXT,
    name           VARCHAR(255)
);
-- constraints
ALTER TABLE devfile_attributes ADD CONSTRAINT fk_devfile_attributes_devfile_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
CREATE INDEX index_devfile_attributes_devfile_id ON devfile_attributes (devfile_id);

-----------------------------------------------------------------------------------------------------------------------------------

-- devfile project source
CREATE TABLE devfile_project_source (
    id         BIGINT       NOT NULL,
    type       VARCHAR(255) NOT NULL,
    location   VARCHAR(255) NOT NULL,
    refspec    VARCHAR(255),

    PRIMARY KEY (id)
);

-- devfile project
CREATE TABLE devfile_project (
    id                   BIGINT       NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    source_id            BIGINT       NOT NULL,
    devfile_projects_id  BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_project ADD CONSTRAINT fk_devfile_projects_id FOREIGN KEY (devfile_projects_id) REFERENCES devfile (id);
ALTER TABLE devfile_project ADD CONSTRAINT fk_devfile_project_source_id FOREIGN KEY (source_id) REFERENCES devfile_project_source (id);
CREATE INDEX index_projects_devfile_id ON devfile_project (devfile_projects_id);

-----------------------------------------------------------------------------------------------------------------------------------

-- devfile command
CREATE TABLE devfile_command (
    id                   BIGINT       NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    devfile_commands_id  BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_command ADD CONSTRAINT fk_devfile_command_id FOREIGN KEY (devfile_commands_id) REFERENCES devfile (id);
CREATE INDEX index_commands_devfile_id ON devfile_command (devfile_commands_id);

-- devfile command attributes
CREATE TABLE devfile_command_attributes (
    command_id     VARCHAR(255),
    value          TEXT,
    name           VARCHAR(255)
);
-- constraints
ALTER TABLE devfile_command_attributes ADD CONSTRAINT fk_devfile_command_attributes_command_id FOREIGN KEY (command_id) REFERENCES devfile_command (id);
CREATE INDEX index_command_attributes_command_id ON devfile_command_attributes (command_id);

-- devfile command action
CREATE TABLE devfile_action (
    id                      BIGINT       NOT NULL,
    type                    VARCHAR(255) NOT NULL,
    component               TEXT,
    command                 TEXT,
    workdir                 TEXT,
    devfile_actions_id      BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_action ADD CONSTRAINT fk_devfile_actions_id FOREIGN KEY (devfile_actions_id) REFERENCES devfile_command (id);
CREATE INDEX index_action_command_id ON devfile_action (devfile_actions_id);


-----------------------------------------------------------------------------------------------------------------------------------

-- devfile component
CREATE TABLE devfile_component (
    id                     BIGINT       NOT NULL,
    name                   VARCHAR(255) NOT NULL,
    type                   VARCHAR(255) NOT NULL,
    reference              TEXT,
    reference_content      TEXT,
    image                  TEXT,
    memory_limit           VARCHAR(255),
    mount_sources          BOOLEAN,
    devfile_components_id  BIGINT,

    PRIMARY KEY (id)
);

ALTER TABLE devfile_component ADD CONSTRAINT fk_devfile_component_id FOREIGN KEY (devfile_components_id) REFERENCES devfile (id);
CREATE INDEX index_component_devfile_id ON devfile_component (devfile_components_id);

---------

CREATE TABLE component_command (
    devfile_component_id    BIGINT,
    commands                VARCHAR(255)
);

--constraints
ALTER TABLE component_command ADD CONSTRAINT fk_component_command_component_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE INDEX index_commands_component_id ON component_command (devfile_component_id);

CREATE TABLE component_arg (
    devfile_component_id    BIGINT,
    args                    VARCHAR(255)
);

--constraints
ALTER TABLE component_arg ADD CONSTRAINT fk_component_command_arg_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE INDEX index_args_component_id ON component_arg (devfile_component_id);

---------

-- devfile endpoint
CREATE TABLE devfile_endpoint (
    id                      BIGINT       NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    port                    INTEGER NOT NULL,
    devfile_endpoints_id    BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_endpoint ADD CONSTRAINT fk_devfile_endpoint_id FOREIGN KEY (devfile_endpoints_id) REFERENCES devfile_component (id);
CREATE INDEX index_devfile_endpoint_id ON devfile_endpoint (devfile_endpoints_id);

-- devfile endpoint attributes
CREATE TABLE devfile_endpoint_attributes (
    endpoint_id    VARCHAR(255),
    value          TEXT,
    name           VARCHAR(255)
);
-- constraints
ALTER TABLE devfile_endpoint_attributes ADD CONSTRAINT fk_devfile_endpoint_attributes_id FOREIGN KEY (endpoint_id) REFERENCES devfile_endpoint (id);
CREATE INDEX index_devfile_endpoint_attributes ON devfile_endpoint_attributes (endpoint_id);

---------

-- devfile component env

CREATE TABLE devfile_env (
    id                  BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    value               TEXT,
    devfile_env_id      BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_env ADD CONSTRAINT fk_devfile_env_id FOREIGN KEY (devfile_env_id) REFERENCES devfile_component (id);
CREATE INDEX index_component_env_id ON devfile_env (devfile_env_id);

---------

-- devfile component volume

CREATE TABLE devfile_volume (
    id                     BIGINT       NOT NULL,
    name                   VARCHAR(255) NOT NULL,
    container_path         TEXT,
    devfile_volumes_id     BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_volume ADD CONSTRAINT fk_devfile_volumes_id FOREIGN KEY (devfile_volumes_id) REFERENCES devfile_component (id);
CREATE INDEX index_component_volume_id ON devfile_volume (devfile_volumes_id);

---------

-- devfile component entrypoint

CREATE TABLE devfile_entrypoint (
    id                         BIGINT       NOT NULL,
    parent_name                VARCHAR(255) NOT NULL,
    container_name             VARCHAR(255) NOT NULL,
    devfile_entrypoints_id     BIGINT,

    PRIMARY KEY (id)
);
ALTER TABLE devfile_entrypoint ADD CONSTRAINT fk_devfile_entrypoints_id FOREIGN KEY (devfile_entrypoints_id) REFERENCES devfile_component (id);
CREATE INDEX index_component_entrypoint_id ON devfile_entrypoint (devfile_entrypoints_id);


CREATE TABLE entrypoint_arg (
    entrypoint_id    BIGINT,
    args             VARCHAR(255)
);

--constraints
ALTER TABLE entrypoint_arg ADD CONSTRAINT fk_entrypoint_arg_id FOREIGN KEY (entrypoint_id) REFERENCES devfile_entrypoint (id);
CREATE INDEX index_entrypoint_arg_entrypoint_id ON entrypoint_arg (entrypoint_id);


CREATE TABLE entrypoint_commands (
    entrypoint_id    BIGINT,
    commands         VARCHAR(255)
);

--constraints
ALTER TABLE entrypoint_commands ADD CONSTRAINT fk_entrypoint_commands_id FOREIGN KEY (entrypoint_id) REFERENCES devfile_entrypoint (id);
CREATE INDEX index_entrypoint_commands_entrypoint_id ON entrypoint_commands (entrypoint_id);


CREATE TABLE entrypoint_selector (
    entrypoint_id    BIGINT,
    selectors_key    VARCHAR(255),
    selectors        VARCHAR(255)
);

--constraints
ALTER TABLE entrypoint_selector ADD CONSTRAINT fk_entrypoint_selector_id FOREIGN KEY (entrypoint_id) REFERENCES devfile_entrypoint (id);
CREATE INDEX index_entrypoint_selector_entrypoint_id ON entrypoint_selector (entrypoint_id);


-----------------------------------------------------------------------------------------------------------------------------------

-- add devfile into workspace
ALTER TABLE workspace ADD COLUMN devfile_id BIGINT;
ALTER TABLE workspace ADD CONSTRAINT fk_workspace_devfile_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
CREATE INDEX index_workspace_devfile_id ON workspace (devfile_id);
