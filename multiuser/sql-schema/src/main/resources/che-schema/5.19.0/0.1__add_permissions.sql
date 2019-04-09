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

-- System permissions ----------------------------------------------------------
CREATE TABLE che_system_permissions (
  id       VARCHAR(255)         NOT NULL,
  user_id  VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_system_permissions_user_id ON che_system_permissions (user_id);
-- constraints
ALTER TABLE che_system_permissions ADD CONSTRAINT che_fk_system_permissions_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- System permissions actions --------------------------------------------------
CREATE TABLE che_system_permissions_actions (
  system_permissions_id   VARCHAR(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_system_permissions_actions_actions ON che_system_permissions_actions (actions);
-- constraints
ALTER TABLE che_system_permissions_actions ADD CONSTRAINT che_fk_system_permissions_actions_system_permissions_id FOREIGN KEY (system_permissions_id) REFERENCES che_system_permissions (id);
--------------------------------------------------------------------------------


-- Workspace workers -----------------------------------------------------------
CREATE TABLE che_worker (
  id               VARCHAR(255)         NOT NULL,
  user_id          VARCHAR(255),
  workspace_id     VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_worker_user_id_workspace_id ON che_worker (user_id, workspace_id);
CREATE INDEX che_index_worker_workspace_id ON che_worker (workspace_id);
-- constraints
ALTER TABLE che_worker ADD CONSTRAINT che_fk_worker_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
ALTER TABLE che_worker ADD CONSTRAINT che_fk_worker_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);
--------------------------------------------------------------------------------


-- Worker actions --------------------------------------------------------------
CREATE TABLE che_worker_actions (
  worker_id       VARCHAR(255),
  actions         VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_worker_actions_actions ON che_worker_actions (actions);
-- constraints
ALTER TABLE che_worker_actions ADD CONSTRAINT che_fk_worker_actions_worker_id FOREIGN KEY (worker_id) REFERENCES che_worker (id);
--------------------------------------------------------------------------------


-- Stack permissions -----------------------------------------------------------
CREATE TABLE che_stack_permissions (
  id           VARCHAR(255)         NOT NULL,
  stack_id     VARCHAR(255),
  user_id      VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_stack_permissions_user_id_stack_id ON che_stack_permissions (user_id, stack_id);
CREATE INDEX che_index_stack_permissions_stack_id ON che_stack_permissions (stack_id);
-- constraints
ALTER TABLE che_stack_permissions ADD CONSTRAINT che_fk_stack_permissions_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
ALTER TABLE che_stack_permissions ADD CONSTRAINT che_fk_stack_permissions_stack_id FOREIGN KEY (stack_id) REFERENCES stack (id);
--------------------------------------------------------------------------------


-- Stack permissions actions ---------------------------------------------------
CREATE TABLE che_stack_permissions_actions (
  stack_permissions_id    VARCHAR(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_stack_permissions_actions_actions ON che_stack_permissions_actions (actions);
-- constraints
ALTER TABLE che_stack_permissions_actions ADD CONSTRAINT che_fk_stack_permissions_actions_stack_permissions_id FOREIGN KEY (stack_permissions_id) REFERENCES che_stack_permissions (id);
--------------------------------------------------------------------------------


-- Recipe permissions ----------------------------------------------------------
CREATE TABLE che_recipe_permissions (
  id           VARCHAR(255)         NOT NULL,
  recipe_id    VARCHAR(255),
  user_id      VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_recipe_permissions_user_id_recipe_id ON che_recipe_permissions (user_id, recipe_id);
CREATE INDEX che_index_recipe_permissions_recipe_id ON che_recipe_permissions (recipe_id);
-- constraints
ALTER TABLE che_recipe_permissions ADD CONSTRAINT che_fk_recipe_permissions_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
ALTER TABLE che_recipe_permissions ADD CONSTRAINT che_fk_recipe_permissions_recipe_id FOREIGN KEY (recipe_id) REFERENCES recipe (id);
--------------------------------------------------------------------------------


-- Recipe permissions actions --------------------------------------------------
CREATE TABLE che_recipe_permissions_actions (
  recipe_permissions_id    varchar(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE index che_index_recipe_permissions_actions_actions ON che_recipe_permissions_actions (actions);
-- constraints
ALTER TABLE che_recipe_permissions_actions ADD CONSTRAINT che_fk_recipe_permissions_actions_recipe_permissions_id FOREIGN KEY (recipe_permissions_id) REFERENCES che_recipe_permissions (id);
--------------------------------------------------------------------------------
