--
-- Copyright (c) 2012-2017 Red Hat, Inc.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

CREATE TABLE workspace_expiration (
    workspace_id               VARCHAR(255),
    expiration                 BIGINT          NOT NULL
);
--constraints
ALTER TABLE workspace_expiration ADD CONSTRAINT ws_expiration_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);
--indexes
CREATE INDEX index_ws_expiration_workspace_id ON workspace_expiration (workspace_id);
CREATE INDEX index_ws_expiration_expiration ON workspace_expiration (expiration);
