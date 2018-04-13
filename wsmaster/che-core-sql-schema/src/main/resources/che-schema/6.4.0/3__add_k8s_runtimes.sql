--
-- Copyright (c) 2012-2018 Red Hat, Inc.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

-- Runtimes --------------------------------------------------------------------
CREATE TABLE che_k8s_runtime (
    workspace_id    VARCHAR(255)    NOT NULL    UNIQUE,
    env_name        VARCHAR(255)    NOT NULL,
    owner_id        VARCHAR(255)    NOT NULL,
    namespace       VARCHAR(255)    NOT NULL,
    status          VARCHAR(255)    NOT NULL,

    PRIMARY KEY (workspace_id, env_name, owner_id)
);
--indexes
CREATE UNIQUE INDEX index_che_k8s_runtime_ws_id ON che_k8s_runtime (workspace_id);
--constraints
ALTER TABLE che_k8s_runtime ADD CONSTRAINT fk_che_k8s_runtime_workspace FOREIGN KEY (workspace_id) REFERENCES workspace (id);
--------------------------------------------------------------------------------


-- Machines ---------------------------------------------------------------------
CREATE TABLE che_k8s_machine (
    workspace_id        VARCHAR(255)    NOT NULL,
    machine_name        VARCHAR(255)    NOT NULL,
    pod_name            VARCHAR(255)    NOT NULL,
    container_name      VARCHAR(255)    NOT NULL,
    status              VARCHAR(255)    NOT NULL,

    PRIMARY KEY (workspace_id, machine_name)
);
--constraints
ALTER TABLE che_k8s_machine ADD CONSTRAINT fk_che_k8s_machine_runtime FOREIGN KEY (workspace_id) REFERENCES che_k8s_runtime (workspace_id);


CREATE TABLE che_k8s_machine_attributes (
    workspace_id    VARCHAR(255),
    machine_name    VARCHAR(255),
    attribute_key   VARCHAR(255),
    attribute       VARCHAR(255)
);
--indexes
CREATE INDEX index_che_k8s_machine_attr_workspace_id_machine_name ON che_k8s_machine_attributes(workspace_id, machine_name);
--constraints
ALTER TABLE che_k8s_machine_attributes ADD CONSTRAINT fk_che_k8s_machine_attributes_machine FOREIGN KEY (workspace_id, machine_name) REFERENCES che_k8s_machine (workspace_id, machine_name);
--------------------------------------------------------------------------------


-- Servers ---------------------------------------------------------------------
CREATE TABLE che_k8s_server (
    workspace_id    VARCHAR(255)    NOT NULL,
    machine_name    VARCHAR(255)    NOT NULL,
    server_name     VARCHAR(255)    NOT NULL,
    url             VARCHAR(255)    NOT NULL,
    status          VARCHAR(255)    NOT NULL,

    PRIMARY KEY (workspace_id, machine_name, server_name)
);

--constraints
ALTER TABLE che_k8s_server ADD CONSTRAINT fk_che_k8s_server_machine FOREIGN KEY (workspace_id, machine_name) REFERENCES che_k8s_machine (workspace_id, machine_name);

CREATE TABLE che_k8s_server_attributes (
    workspace_id    VARCHAR(255),
    machine_name    VARCHAR(255),
    server_name     VARCHAR(255),
    attribute_key   VARCHAR(255),
    attribute       VARCHAR(255)
);
--indexes
CREATE INDEX index_che_k8s_server_attr_ws_id_machine_name ON che_k8s_server_attributes(workspace_id, machine_name, server_name);
--constraints
ALTER TABLE che_k8s_server_attributes ADD CONSTRAINT fk_che_k8s_server_attributes_machine FOREIGN KEY (workspace_id, machine_name, server_name) REFERENCES che_k8s_server (workspace_id, machine_name, server_name);
--------------------------------------------------------------------------------
