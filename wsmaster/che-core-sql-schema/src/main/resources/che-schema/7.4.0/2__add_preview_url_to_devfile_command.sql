--
-- Copyright (c) 2012-2019 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--


ALTER TABLE devfile_command ADD COLUMN preview_url_port INTEGER;
ALTER TABLE devfile_command ADD COLUMN preview_url_path TEXT;

ALTER TABLE k8s_runtime_command ADD COLUMN preview_url_port INTEGER;
ALTER TABLE k8s_runtime_command ADD COLUMN preview_url_path TEXT;

ALTER TABLE command ADD COLUMN preview_url_port INTEGER;
ALTER TABLE command ADD COLUMN preview_url_path TEXT;
