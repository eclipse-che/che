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

-- Allow null as defaultEnv field in workspace config
ALTER TABLE workspaceconfig MODIFY COLUMN defaultenv VARCHAR(255);

ALTER TABLE che_k8s_runtime DROP PRIMARY KEY;
ALTER TABLE che_k8s_runtime ADD PRIMARY KEY (workspace_id);

-- Allow null as env_name field in kubernetes runtime
ALTER TABLE che_k8s_runtime MODIFY COLUMN env_name VARCHAR(255);
