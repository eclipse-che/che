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

CREATE TABLE che_workspace_activity (
    workspace_id               VARCHAR(255) NOT NULL,
    status                     VARCHAR(255),
    created                    BIGINT,
    last_starting              BIGINT,
    last_running               BIGINT,
    last_stopping              BIGINT,
    last_stopped               BIGINT,
    expiration                 BIGINT,

    PRIMARY KEY (workspace_id)
);
ALTER TABLE che_workspace_activity ADD CONSTRAINT ws_activity_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);
CREATE INDEX che_index_ws_activity_last_starting ON che_workspace_activity (status, last_starting);
CREATE INDEX che_index_ws_activity_last_running ON che_workspace_activity (status, last_running);
CREATE INDEX che_index_ws_activity_last_stopping ON che_workspace_activity (status, last_stopping);
CREATE INDEX che_index_ws_activity_last_stopped ON che_workspace_activity (status, last_stopped);
CREATE INDEX che_index_ws_activity_expiration ON che_workspace_activity (expiration);
