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

INSERT INTO che_workspace_activity (workspace_id, created, expiration, status, last_running, last_stopped)
  SELECT a.workspace_id, a.attributes, e.expiration,
    CASE
      WHEN r.status = '0' THEN 'STARTING'
      WHEN r.status = '1' THEN 'RUNNING'
      WHEN r.status = '2' THEN 'STOPPING'
      ELSE 'STOPPED' -- also handles the lack of explicit status
    END,
    a_forRunning.attributes, a_forStopped.attributes
  FROM workspace_attributes AS a
  -- pull in the existing expiration times
  LEFT JOIN che_workspace_expiration AS e
    ON a.workspace_id = e.workspace_id
  -- pull in the recorded current status of the workspaces
  LEFT JOIN che_k8s_runtime AS r
    ON a.workspace_id = r.workspace_id
  -- consider the 'updated' time of a running workspace as its "last_running" event time
  LEFT JOIN workspace_attributes AS a_forRunning
    ON a.workspace_id = a_forRunning.workspace_id
    AND r.status = 'RUNNING'
    AND a_forRunning.attributes_key = 'updated'
  -- pick up the 'stopped' timestamp from the workspace attributes (if any)
  LEFT JOIN workspace_attributes AS a_forStopped
    ON a.workspace_id = a_forStopped.workspace_id
    AND a_forStopped.attributes_key = 'stopped'
  -- we're basing all of the above on workspaces that have the 'created' attribute that stores the
  -- timestamp when the workspace was created
  WHERE a.attributes_key = 'created';
