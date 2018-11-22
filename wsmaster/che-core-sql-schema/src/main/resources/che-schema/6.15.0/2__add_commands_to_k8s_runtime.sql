--
-- Copyright (c) 2012-2018 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--


-- Commands --------------------------------------------------------------------
CREATE TABLE k8s_runtime_command (
    id              BIGINT         NOT NULL,
    commandline     TEXT,
    name            VARCHAR(255)   NOT NULL,
    type            VARCHAR(255)   NOT NULL,
    workspace_id    VARCHAR(255),

    PRIMARY KEY (id)
);
--indexes
CREATE INDEX index_k8s_runtime_command_ws_id ON k8s_runtime_command (workspace_id);
--constraints
ALTER TABLE k8s_runtime_command ADD CONSTRAINT fk_k8s_runtime_workspace_id FOREIGN KEY (workspace_id) REFERENCES che_k8s_runtime (workspace_id);
--------------------------------------------------------------------------------


-- Command attributes ----------------------------------------------------------
CREATE TABLE k8s_runtime_command_attributes (
    command_id  BIGINT             NOT NULL,
    name        VARCHAR(255)       NOT NULL,
    value       TEXT
);
--indexes
CREATE INDEX index_k8s_runtime_command_attr_command_id ON k8s_runtime_command_attributes (command_id);
--constraints
ALTER TABLE k8s_runtime_command_attributes ADD CONSTRAINT fk_k8s_runtime_command_attr_command_id FOREIGN KEY (command_id) REFERENCES k8s_runtime_command (id);
--------------------------------------------------------------------------------
