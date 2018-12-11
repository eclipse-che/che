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
INSERT INTO che_workspace_activity (workspace_id, expiration)
  SELECT workspace_id, expiration FROM che_workspace_expiration;

INSERT INTO che_workspace_activity (workspace_id, created)
  SELECT workspace_id, cast (attributes as bigint) FROM workspace_attributes
  WHERE attributes_key = 'created';

UPDATE che_workspace_activity SET status = r.status
  FROM che_k8s_runtime r
  WHERE che_workspace_activity.workspace_id = r.workspace_id;

UPDATE che_workspace_activity SET last_running = cast(a.attributes as bigint)
  FROM workspace_attributes AS a, che_k8s_runtime r
  WHERE che_workspace_activity.workspace_id = a.workspace_id AND a.workspace_id = r.workspace_id
  AND a.attributes_key = 'updated' AND r.status = 'RUNNING';

UPDATE che_workspace_activity SET last_stopped = cast(a.attributes as bigint)
  FROM workspace_attributes AS a WHERE che_workspace_activity.workspace_id = a.workspace_id
  AND a.attributes_key LIKE 'stopped%';
