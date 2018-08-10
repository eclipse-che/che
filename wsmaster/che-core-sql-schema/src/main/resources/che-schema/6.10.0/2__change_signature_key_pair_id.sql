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

-- Remove old data
TRUNCATE TABLE che_sign_key_pair;

-- Rename field
ALTER TABLE che_sign_key_pair RENAME COLUMN id TO workspace_id;

-- Add workspace reference
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);

-- Index
CREATE INDEX index_sign_private_key_pair_id ON che_sign_key_pair (workspace_id);
