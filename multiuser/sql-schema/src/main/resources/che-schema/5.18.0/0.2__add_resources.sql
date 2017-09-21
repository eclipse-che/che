--
--  [2012] - [2017] Codenvy, S.A.
--  All Rights Reserved.
--
-- NOTICE:  All information contained herein is, and remains
-- the property of Codenvy S.A. and its suppliers,
-- if any.  The intellectual and technical concepts contained
-- herein are proprietary to Codenvy S.A.
-- and its suppliers and may be covered by U.S. and Foreign Patents,
-- patents in process, and are protected by trade secret or copyright law.
-- Dissemination of this information or reproduction of this material
-- is strictly forbidden unless prior written permission is obtained
-- from Codenvy S.A..
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
ALTER TABLE che_free_resources_limit ADD CONSTRAINT fk_free_resources_limit_account_id FOREIGN KEY (account_id) REFERENCES account (id);
--------------------------------------------------------------------------------


-- Free resource limit resource ------------------------------------------------
CREATE TABLE che_free_resources_limit_resource (
    free_resources_limit_account_id        VARCHAR(255)    NOT NULL,
    resources_id                           BIGINT          NOT NULL,

    PRIMARY KEY (free_resources_limit_account_id, resources_id)
);
-- constraints
ALTER TABLE che_free_resources_limit_resource ADD CONSTRAINT fk_free_resources_limit_resource_resources_id FOREIGN KEY (resources_id) REFERENCES che_resource (id);
ALTER TABLE che_free_resources_limit_resource ADD CONSTRAINT fk_free_resources_limit_resource_account_id FOREIGN KEY (free_resources_limit_account_id) REFERENCES che_free_resources_limit (account_id);
--------------------------------------------------------------------------------
