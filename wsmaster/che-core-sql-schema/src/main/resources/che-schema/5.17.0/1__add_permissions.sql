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

-- System permissions ----------------------------------------------------------
CREATE TABLE che_systempermissions (
  id      VARCHAR(255)         NOT NULL,
  userid  VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_systempermissions_userid ON che_systempermissions (userid);
-- constraints
ALTER TABLE che_systempermissions ADD CONSTRAINT che_fk_systempermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- System permissions actions --------------------------------------------------
CREATE TABLE che_systempermissions_actions (
  systempermissions_id    VARCHAR(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_systempermissions_actions_actions ON che_systempermissions_actions (actions);
-- constraints
ALTER TABLE che_systempermissions_actions ADD CONSTRAINT che_fk_systempermissions_actions_systempermissions_id FOREIGN KEY (systempermissions_id) REFERENCES che_systempermissions (id);
--------------------------------------------------------------------------------


-- Workspace workers -----------------------------------------------------------
CREATE TABLE che_worker (
  id              VARCHAR(255)         NOT NULL,
  userid          VARCHAR(255),
  workspaceid     VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_worker_userid_workspaceid ON che_worker (userid, workspaceid);
CREATE INDEX che_index_worker_workspaceid ON che_worker (workspaceid);
-- constraints
ALTER TABLE che_worker ADD CONSTRAINT che_fk_worker_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE che_worker ADD CONSTRAINT che_fk_worker_workspaceid FOREIGN KEY (workspaceid) REFERENCES workspace (id);
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
CREATE TABLE che_stackpermissions (
  id          VARCHAR(255)         NOT NULL,
  stackid     VARCHAR(255),
  userid      VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_stackpermissions_userid_stackid ON che_stackpermissions (userid, stackid);
CREATE INDEX che_index_stackpermissions_stackid ON che_stackpermissions (stackid);
-- constraints
ALTER TABLE che_stackpermissions ADD CONSTRAINT che_fk_stackpermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE che_stackpermissions ADD CONSTRAINT che_fk_stackpermissions_stackid FOREIGN KEY (stackid) REFERENCES stack (id);
--------------------------------------------------------------------------------


-- Stack permissions actions ---------------------------------------------------
CREATE TABLE che_stackpermissions_actions (
  stackpermissions_id     VARCHAR(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX che_index_stackpermissions_actions_actions ON che_stackpermissions_actions (actions);
-- constraints
ALTER TABLE che_stackpermissions_actions ADD CONSTRAINT che_fk_stackpermissions_actions_stackpermissions_id FOREIGN KEY (stackpermissions_id) REFERENCES che_stackpermissions (id);
--------------------------------------------------------------------------------


-- Recipe permissions ----------------------------------------------------------
CREATE TABLE che_recipepermissions (
  id          VARCHAR(255)         NOT NULL,
  recipeid    VARCHAR(255),
  userid      VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX che_index_recipepermissions_userid_recipeid ON che_recipepermissions (userid, recipeid);
CREATE INDEX che_index_recipepermissions_recipeid ON che_recipepermissions (recipeid);
-- constraints
ALTER TABLE che_recipepermissions ADD CONSTRAINT che_fk_recipepermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE che_recipepermissions ADD CONSTRAINT che_fk_recipepermissions_recipeid FOREIGN KEY (recipeid) REFERENCES recipe (id);
--------------------------------------------------------------------------------


-- Recipe permissions actions --------------------------------------------------
create table che_recipepermissions_actions (
  recipepermissions_id    varchar(255),
  actions                 VARCHAR(255)
);
-- indexes
create index che_index_recipepermissions_actions_actions on che_recipepermissions_actions (actions);
-- constraints
ALTER TABLE che_recipepermissions_actions ADD CONSTRAINT che_fk_recipepermissions_actions_recipepermissions_id FOREIGN KEY (recipepermissions_id) REFERENCES che_recipepermissions (id);
--------------------------------------------------------------------------------
