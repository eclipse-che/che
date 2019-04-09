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

-- Organization ----------------------------------------------------------------
CREATE TABLE che_organization (
    id          VARCHAR(255)         NOT NULL,
    parent      VARCHAR(255),
    account_id  VARCHAR(255)         NOT NULL,

    PRIMARY KEY (id)
);
-- indexes
CREATE INDEX che_index_organization_parent ON che_organization (parent);
-- constraints
ALTER TABLE che_organization ADD CONSTRAINT che_fk_organization_parent FOREIGN KEY (parent) REFERENCES che_organization (id);
ALTER TABLE che_organization ADD CONSTRAINT che_fk_organization_account_id FOREIGN KEY (account_id) REFERENCES account (id);
--------------------------------------------------------------------------------


-- Organization member ---------------------------------------------------------
CREATE TABLE che_member (
    id              VARCHAR(255)         NOT NULL,
    organization_id  VARCHAR(255),
    user_id          VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_member_user_id_organization_id ON che_member (user_id, organization_id);
CREATE INDEX che_index_member_organization_id ON che_member (organization_id);
-- constraints
ALTER TABLE che_member ADD CONSTRAINT che_fk_member_organization_id FOREIGN KEY (organization_id) REFERENCES che_organization (id);
ALTER TABLE che_member ADD CONSTRAINT che_fk_member_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
--------------------------------------------------------------------------------


--Member actions ---------------------------------------------------------------
CREATE TABLE che_member_actions (
    member_id       VARCHAR(255),
    actions         VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_member_actions_actions ON che_member_actions (actions);
-- constraints
ALTER TABLE che_member_actions ADD CONSTRAINT che_fk_member_actions_member_id FOREIGN KEY (member_id) REFERENCES che_member (id);
--------------------------------------------------------------------------------

-- Organization distributed resources  -----------------------------------------------------------
CREATE TABLE che_organization_distributed_resources (
    organization_id       VARCHAR(255)         NOT NULL,

    PRIMARY KEY (organization_id)
);
-- constraints
ALTER TABLE che_organization_distributed_resources ADD CONSTRAINT che_fk_org_distributed_res_organization_id FOREIGN KEY (organization_id) REFERENCES che_organization (id);
--------------------------------------------------------------------------------


-- Organization distributed resources to resource ------------------------------------------------
CREATE TABLE che_organization_distributed_resources_resource (
    organization_distributed_resources_id               VARCHAR(255)    NOT NULL,
    resource_id                                         BIGINT          NOT NULL,

    PRIMARY KEY (organization_distributed_resources_id, resource_id)
);
-- constraints
ALTER TABLE che_organization_distributed_resources_resource ADD CONSTRAINT che_fk_org_distributed_res_resource_resource_id FOREIGN KEY (resource_id) REFERENCES che_resource (id);
ALTER TABLE che_organization_distributed_resources_resource ADD CONSTRAINT che_fk_org_distributed_res_resource_org_distributed_res_id FOREIGN KEY (organization_distributed_resources_id) REFERENCES che_organization_distributed_resources (organization_id);
--------------------------------------------------------------------------------
