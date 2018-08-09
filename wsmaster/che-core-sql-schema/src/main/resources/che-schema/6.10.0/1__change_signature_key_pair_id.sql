--
-- Copyright (c) 2012-2017 Red Hat, Inc.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
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