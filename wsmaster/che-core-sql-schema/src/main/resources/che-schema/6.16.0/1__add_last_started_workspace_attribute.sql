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

-- A workspace can be merely created and not started and therefore the code doesn't assume that
-- a 'started' attribute exists. Therefore it is OK for us to create a started attribute only for
-- the currently running workspaces. For those there is a certain probability that the last update
-- was indeed the actual start of the workspace, not its update.
INSERT INTO workspace_attributes (workspace_id, attributes, attribute_key)
  SELECT a.workspace_id, a.attributes, 'started' FROM workspace_attributes a, che_k8s_runtime r
  WHERE r.workspace_id = a.workspace_id AND r.status <> 'STOPPED' AND a.attribute_key = 'updated';
