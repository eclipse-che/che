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

-- devfile command action references
ALTER TABLE devfile_action MODIFY COLUMN component VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE devfile_action MODIFY COLUMN command TEXT NULL DEFAULT NULL;
ALTER TABLE devfile_action ADD COLUMN reference TEXT;
ALTER TABLE devfile_action ADD COLUMN reference_content TEXT;
