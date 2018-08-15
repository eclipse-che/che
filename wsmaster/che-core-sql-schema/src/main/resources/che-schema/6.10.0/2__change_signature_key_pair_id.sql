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

-- Rename old table
ALTER TABLE che_sign_key_pair RENAME TO che_sign_key_pair_old;

-- Create new key pair table
CREATE TABLE che_sign_key_pair (
    workspace_id        VARCHAR(255) NOT NULL,
    public_key          BIGINT       NOT NULL,
    private_key         BIGINT       NOT NULL,

    PRIMARY KEY (workspace_id)
);
-- Constraint
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);

-- Copy data
INSERT  INTO che_sign_key_pair
SELECT r.workspace_id, k.public_key, k.private_key
FROM che_k8s_runtime AS r,
(SELECT public_key, private_key FROM che_sign_key_pair_old LIMIT 1) AS k;

-- Cleanup
DROP TABLE che_sign_key_pair_old CASCADE;
