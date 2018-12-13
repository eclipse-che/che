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

-- We specialize for postgres and mysql, leaving this default for H2.

INSERT INTO che_workspace_activity (workspace_id, created)
  SELECT workspace_id, cast(attributes as bigint) FROM workspace_attributes
  WHERE attributes_key = 'created';

UPDATE che_workspace_activity AS a SET expiration =
  (SELECT expiration FROM che_workspace_expiration e WHERE a.workspace_id = e.workspace_id);

UPDATE che_workspace_activity AS a SET status =
  (SELECT r.status FROM che_k8s_runtime r WHERE a.workspace_id = r.workspace_id);

UPDATE che_workspace_activity AS a SET last_running =
  (SELECT cast(t.attributes as bigint) FROM workspace_attributes AS t, che_k8s_runtime AS r
    WHERE a.workspace_id = t.workspace_id AND a.workspace_id = r.workspace_id);

UPDATE che_workspace_activity AS a SET last_stopped =
  (SELECT cast(t.attributes as bigint) FROM workspace_attributes AS t
    WHERE a.workspace_id = t.workspace_id AND t.attributes_key LIKE 'stopped%');
