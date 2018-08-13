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
-- Constraints
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_public_key_id FOREIGN KEY (public_key) REFERENCES che_sign_key (id);
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_private_key_id FOREIGN KEY (private_key) REFERENCES che_sign_key (id);
-- Indexes
CREATE INDEX index_sign_private_key_pair_id ON che_sign_key_pair (workspace_id);
CREATE INDEX index_sign_public_key_id ON che_sign_key_pair (public_key);
CREATE INDEX index_sign_private_key_id ON che_sign_key_pair (private_key);

-- Copy data
INSERT  INTO che_sign_key_pair
WITH q1 AS (select workspace_id from che_k8s_runtime)
   , q2 AS (select public_key, private_key from che_k8s_runtime, che_sign_key_pair_old LIMIT 1)
SELECT * FROM q1, q2;

