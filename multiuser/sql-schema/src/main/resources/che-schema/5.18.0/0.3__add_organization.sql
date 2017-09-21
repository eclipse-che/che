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

-- Organization ----------------------------------------------------------------
CREATE TABLE che_organization (
    id          VARCHAR(255)         NOT NULL,
    parent      VARCHAR(255),
    account_id  VARCHAR(255)         NOT NULL,

    PRIMARY KEY (id)
);
-- indexes
CREATE INDEX index_organization_parent ON che_organization (parent);
-- constraints
ALTER TABLE che_organization ADD CONSTRAINT fk_organization_parent FOREIGN KEY (parent) REFERENCES che_organization (id);
ALTER TABLE che_organization ADD CONSTRAINT fk_organization_account_id FOREIGN KEY (account_id) REFERENCES account (id);
--------------------------------------------------------------------------------


-- Organization member ---------------------------------------------------------
CREATE TABLE che_member (
    id              VARCHAR(255)         NOT NULL,
    organization_id  VARCHAR(255),
    user_id          VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_member_user_id_organization_id ON che_member (user_id, organization_id);
CREATE INDEX index_member_organization_id ON che_member (organization_id);
-- constraints
ALTER TABLE che_member ADD CONSTRAINT fk_member_organization_id FOREIGN KEY (organization_id) REFERENCES che_organization (id);
ALTER TABLE che_member ADD CONSTRAINT fk_member_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
--------------------------------------------------------------------------------


--Member actions ---------------------------------------------------------------
CREATE TABLE che_member_actions (
    member_id       VARCHAR(255),
    actions         VARCHAR(255)
);
-- indexes
CREATE INDEX index_member_actions_actions ON che_member_actions (actions);
-- constraints
ALTER TABLE che_member_actions ADD CONSTRAINT fk_member_actions_member_id FOREIGN KEY (member_id) REFERENCES che_member (id);
--------------------------------------------------------------------------------

-- Organization distributed resources  -----------------------------------------------------------
CREATE TABLE che_organization_distributed_resources (
    organization_id       VARCHAR(255)         NOT NULL,

    PRIMARY KEY (organization_id)
);
-- constraints
ALTER TABLE che_organization_distributed_resources ADD CONSTRAINT fk_organization_distributed_resources_organization_id FOREIGN KEY (organization_id) REFERENCES che_organization (id);
--------------------------------------------------------------------------------


-- Organization distributed resources to resource ------------------------------------------------
CREATE TABLE che_organization_distributed_resources_resource (
    organization_distributed_resources_id               VARCHAR(255)    NOT NULL,
    resource_id                                         BIGINT          NOT NULL,

    PRIMARY KEY (organization_distributed_resources_id, resource_id)
);
-- constraints
ALTER TABLE che_organization_distributed_resources_resource ADD CONSTRAINT fk_organization_distributed_resources_resource_resource_id FOREIGN KEY (resource_id) REFERENCES che_resource (id);
ALTER TABLE che_organization_distributed_resources_resource ADD CONSTRAINT fk_organization_distributed_resources_resource_organization_distributed_resources_id FOREIGN KEY (organization_distributed_resources_id) REFERENCES che_organization_distributed_resources (organization_id);
--------------------------------------------------------------------------------
