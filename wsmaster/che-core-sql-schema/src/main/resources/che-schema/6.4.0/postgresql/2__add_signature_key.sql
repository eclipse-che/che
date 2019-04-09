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

-- Signature key ---------------------------------------------------------------------
CREATE TABLE che_sign_key (
    id                    BIGINT       NOT NULL,
    algorithm             VARCHAR(255) NOT NULL,
    encoding_format       VARCHAR(255) NOT NULL,
    encoded_value         BYTEA        NOT NULL,

    PRIMARY KEY (id)
);

-- Signature key pair ----------------------------------------------------------------
CREATE TABLE che_sign_key_pair (
    id                  VARCHAR(255) NOT NULL,
    public_key          BIGINT       NOT NULL,
    private_key         BIGINT       NOT NULL,

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_public_key_id FOREIGN KEY (public_key) REFERENCES che_sign_key (id);
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_private_key_id FOREIGN KEY (private_key) REFERENCES che_sign_key (id);
--indexes
CREATE INDEX index_sign_public_key_id ON che_sign_key_pair (public_key);
CREATE INDEX index_sign_private_key_id ON che_sign_key_pair (private_key);
