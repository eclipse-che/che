--
-- Copyright (c) 2012-2020 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

-- User devfile permissions -----------------------------------------------------------
CREATE TABLE che_userdevfile_permissions (
  id               VARCHAR(255)         NOT NULL,
  user_id          VARCHAR(255),
  userdevfile_id     VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_userdevfile_permissions_user_id_userdevfile_id ON che_userdevfile_permissions (user_id, userdevfile_id);
CREATE INDEX che_index_userdevfile_permissions_userdevfile_id ON che_userdevfile_permissions (userdevfile_id);
-- constraints
ALTER TABLE che_userdevfile_permissions ADD CONSTRAINT che_fk_userdevfile_permissions_user_id FOREIGN KEY (user_id) REFERENCES usr (id);
ALTER TABLE che_userdevfile_permissions ADD CONSTRAINT che_fk_userdevfile_permissions_workspace_id FOREIGN KEY (userdevfile_id) REFERENCES userdevfile (id);
--------------------------------------------------------------------------------


-- User devfile permission actions --------------------------------------------------------------
CREATE TABLE che_userdevfile_permissions_actions (
  userdevfile_permissions_id       VARCHAR(255),
  actions         VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_userdevfile_permissions_actions_actions ON che_userdevfile_permissions_actions (actions);
CREATE INDEX che_index_userdevfile_permissions_actions_userdevfile_id ON che_userdevfile_permissions_actions (userdevfile_permissions_id);
-- constraints
ALTER TABLE che_userdevfile_permissions_actions ADD CONSTRAINT che_fk_userdevfile_permissions_actions_id FOREIGN KEY (userdevfile_permissions_id) REFERENCES che_userdevfile_permissions(id);
--------------------------------------------------------------------------------
