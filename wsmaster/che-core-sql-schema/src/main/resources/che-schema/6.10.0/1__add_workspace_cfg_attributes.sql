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

--Workspace config attributes ---------------------------------------------------------
CREATE TABLE che_workspace_cfg_attributes (
    workspace_id    BIGINT,
    attributes      VARCHAR(255),
    attributes_key  VARCHAR(255)
);

--constraints
ALTER TABLE che_workspace_cfg_attributes ADD CONSTRAINT fk_che_workspace_cfg_attr_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspaceconfig (id);
--------------------------------------------------------------------------------

--indexes
CREATE INDEX index_che_workspace_cfg_attributes_ws_id ON che_workspace_cfg_attributes (workspace_id);
--------------------------------------------------------------------------------
