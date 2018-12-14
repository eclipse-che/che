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

-- copy data from the old workspace expiration table. Leave the old table and data intact, we just
-- won't be using it anymore, but leave it there so that we don't make breaking schema changes
-- straight away.
INSERT INTO che_workspace_activity (workspace_id, created)
  SELECT workspace_id, attributes FROM workspace_attributes
  WHERE attributes_key = 'created';

UPDATE che_workspace_activity AS a
  INNER JOIN che_workspace_expiration AS e
    ON a.workspace_id = e.workspace_id
  SET a.expiration = e.expiration;

UPDATE che_workspace_activity
  INNER JOIN workspace_attributes AS a
    ON che_workspace_activity.workspace_id = a.workspace_id
  INNER JOIN che_k8s_runtime AS r
    ON a.workspace_id = r.workspace_id AND a.attributes_key = 'updated' AND r.status = 'RUNNING'
  SET last_running = a.attributes;

UPDATE che_workspace_activity
  INNER JOIN workspace_attributes AS a
    ON che_workspace_activity.workspace_id = a.workspace_id AND a.attributes_key LIKE 'stopped%'
  SET last_stopped = a.attributes;
