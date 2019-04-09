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

-- Resource --------------------------------------------------------------------
CREATE TABLE che_resource (
    id          BIGINT          NOT NULL,
    amount      BIGINT,
    type        VARCHAR(255)    NOT NULL,
    unit        VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------

-- Free resource limit ---------------------------------------------------------
CREATE TABLE che_free_resources_limit (
    account_id       VARCHAR(255)         NOT NULL,

    PRIMARY KEY (account_id)
);
-- constraints
ALTER TABLE che_free_resources_limit ADD CONSTRAINT che_fk_free_resources_limit_account_id FOREIGN KEY (account_id) REFERENCES account (id);
--------------------------------------------------------------------------------


-- Free resource limit resource ------------------------------------------------
CREATE TABLE che_free_resources_limit_resource (
    free_resources_limit_account_id        VARCHAR(255)    NOT NULL,
    resources_id                           BIGINT          NOT NULL,

    PRIMARY KEY (free_resources_limit_account_id, resources_id)
);
-- constraints
ALTER TABLE che_free_resources_limit_resource ADD CONSTRAINT che_fk_free_resources_limit_resource_resources_id FOREIGN KEY (resources_id) REFERENCES che_resource (id);
ALTER TABLE che_free_resources_limit_resource ADD CONSTRAINT che_fk_free_resources_limit_resource_account_id FOREIGN KEY (free_resources_limit_account_id) REFERENCES che_free_resources_limit (account_id);
--------------------------------------------------------------------------------
