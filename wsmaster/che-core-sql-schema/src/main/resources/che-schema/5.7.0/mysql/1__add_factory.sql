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

-- Factory button --------------------------------------------------------------
CREATE TABLE che_factory_button (
    id          BIGINT          NOT NULL,
    type        VARCHAR(255),
    color       VARCHAR(255),
    counter     BOOLEAN,
    logo        VARCHAR(255),
    style       VARCHAR(255),

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Factory action --------------------------------------------------------------
CREATE TABLE che_factory_action (
    entity_id       BIGINT          NOT NULL,
    id              VARCHAR(255),

    PRIMARY KEY (entity_id)
);
--------------------------------------------------------------------------------


-- Factory action properties ---------------------------------------------------
CREATE TABLE che_factory_action_properties (
    action_entity_id        BIGINT          NOT NULL,
    property_value          VARCHAR(255),
    property_key            VARCHAR(255)
);
-- constraints
ALTER TABLE che_factory_action_properties ADD CONSTRAINT fk_che_f_action_props_action_entity_id FOREIGN KEY (action_entity_id) REFERENCES che_factory_action (entity_id);
CREATE INDEX che_f_action_entity_id_fk ON che_factory_action_properties (action_entity_id);
--------------------------------------------------------------------------------


-- Factory on app closed action ------------------------------------------------
CREATE TABLE che_factory_on_app_closed_action (
    id      BIGINT      NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Factory on projects loaded action -------------------------------------------
CREATE TABLE che_factory_on_projects_loaded_action (
    id      BIGINT      NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Factory on app loaded action ------------------------------------------------
CREATE TABLE che_factory_on_app_loaded_action (
    id      BIGINT      NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Factory on app closed action value ------------------------------------------
CREATE TABLE che_factory_on_app_closed_action_value (
    on_app_closed_id    BIGINT      NOT NULL,
    action_entity_id    BIGINT      NOT NULL,

    PRIMARY KEY (on_app_closed_id, action_entity_id)
);
-- constraints
ALTER TABLE che_factory_on_app_closed_action_value ADD CONSTRAINT fk_che_f_on_app_closed_entity_id FOREIGN KEY (action_entity_id) REFERENCES che_factory_action (entity_id);
ALTER TABLE che_factory_on_app_closed_action_value ADD CONSTRAINT fk_che_f_on_app_closed_action_id FOREIGN KEY (on_app_closed_id) REFERENCES che_factory_on_app_closed_action (id);
--------------------------------------------------------------------------------


-- Factory on project loaded action --------------------------------------------
CREATE TABLE che_factory_on_projects_loaded_action_value (
    on_projects_loaded_id       BIGINT      NOT NULL,
    action_entity_id            BIGINT      NOT NULL,

    PRIMARY KEY (on_projects_loaded_id, action_entity_id)
);
-- constraints
ALTER TABLE che_factory_on_projects_loaded_action_value ADD CONSTRAINT fk_che_f_on_projects_loaded_entity_id FOREIGN KEY (action_entity_id) REFERENCES che_factory_action (entity_id);
ALTER TABLE che_factory_on_projects_loaded_action_value ADD CONSTRAINT fk_che_f_on_projects_loaded_action_id FOREIGN KEY (on_projects_loaded_id) REFERENCES che_factory_on_projects_loaded_action (id);
--------------------------------------------------------------------------------


-- Factory on app loaded action ------------------------------------------------
CREATE TABLE che_factory_on_app_loaded_action_value (
    on_app_loaded_id       BIGINT      NOT NULL,
    action_entity_id       BIGINT      NOT NULL,

    PRIMARY KEY (on_app_loaded_id, action_entity_id)
);
-- constraints
ALTER TABLE che_factory_on_app_loaded_action_value ADD CONSTRAINT fk_che_f_on_app_loaded_entity_id FOREIGN KEY (action_entity_id) REFERENCES che_factory_action (entity_id);
ALTER TABLE che_factory_on_app_loaded_action_value ADD CONSTRAINT fk_che_f_on_app_loaded_action_id FOREIGN KEY (on_app_loaded_id) REFERENCES che_factory_on_app_loaded_action (id);
--------------------------------------------------------------------------------


-- Factory ide -----------------------------------------------------------------
CREATE TABLE che_factory_ide (
    id                      BIGINT      NOT NULL,
    on_app_closed_id        BIGINT,
    on_app_loaded_id        BIGINT,
    on_projects_loaded_id   BIGINT,

    PRIMARY KEY (id)
);
-- constraints
ALTER TABLE che_factory_ide ADD CONSTRAINT fk_che_f_ide_on_app_closed_id FOREIGN KEY (on_app_closed_id) REFERENCES che_factory_on_app_closed_action (id);
ALTER TABLE che_factory_ide ADD CONSTRAINT fk_che_f_ide_on_projects_loaded_id FOREIGN KEY (on_projects_loaded_id) REFERENCES che_factory_on_projects_loaded_action (id);
ALTER TABLE che_factory_ide ADD CONSTRAINT fk_che_f_ide_on_app_loaded_id FOREIGN KEY (on_app_loaded_id) REFERENCES che_factory_on_app_loaded_action (id);
--------------------------------------------------------------------------------


-- Factory ---------------------------------------------------------------------
CREATE TABLE che_factory (
    id                  VARCHAR(255)         NOT NULL,
    name                VARCHAR(255),
    version             VARCHAR(255)         NOT NULL,
    created             BIGINT,
    user_id             VARCHAR(255),
    creation_strategy   VARCHAR(255),
    match_reopen        VARCHAR(255),
    referrer            VARCHAR(255),
    since               BIGINT,
    until               BIGINT,
    button_id           BIGINT,
    ide_id              BIGINT,
    workspace_id        BIGINT,

    PRIMARY KEY (id)
);
-- constraints
ALTER TABLE che_factory ADD CONSTRAINT fk_che_f_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
ALTER TABLE che_factory ADD CONSTRAINT fk_che_f_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspaceconfig (id);
ALTER TABLE che_factory ADD CONSTRAINT fk_che_f_button_id FOREIGN KEY (button_id) REFERENCES che_factory_button (id);
ALTER TABLE che_factory ADD CONSTRAINT fk_che_f_ide_id FOREIGN KEY (ide_id) REFERENCES che_factory_ide (id);
CREATE UNIQUE INDEX index_che_factory_name_user_id ON che_factory (user_id, name);
--------------------------------------------------------------------------------


-- Factory Images --------------------------------------------------------------
CREATE TABLE che_factory_image (
    image_data   BLOB,
    media_type   VARCHAR(255),
    name         VARCHAR(255),
    factory_id   VARCHAR(255)   NOT NULL
);
-- constraints
ALTER TABLE che_factory_image ADD CONSTRAINT fk_che_factory_image_factory_id FOREIGN KEY (factory_id) REFERENCES che_factory (id);
--------------------------------------------------------------------------------
