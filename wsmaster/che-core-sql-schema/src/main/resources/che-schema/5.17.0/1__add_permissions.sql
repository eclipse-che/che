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
CREATE TABLE systempermissions (
  id      VARCHAR(255)         NOT NULL,
  userid  VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_systempermissions_userid ON systempermissions (userid);
-- constraints
ALTER TABLE systempermissions ADD CONSTRAINT fk_systempermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- System permissions actions --------------------------------------------------
CREATE TABLE systempermissions_actions (
  systempermissions_id    VARCHAR(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX index_systempermissions_actions_actions ON systempermissions_actions (actions);
-- constraints
ALTER TABLE systempermissions_actions ADD CONSTRAINT fk_systempermissions_actions_systempermissions_id FOREIGN KEY (systempermissions_id) REFERENCES systempermissions (id);
--------------------------------------------------------------------------------


-- Workspace workers -----------------------------------------------------------
CREATE TABLE worker (
  id              VARCHAR(255)         NOT NULL,
  userid          VARCHAR(255),
  workspaceid     VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_worker_userid_workspaceid ON worker (userid, workspaceid);
CREATE INDEX index_worker_workspaceid ON worker (workspaceid);
-- constraints
ALTER TABLE worker ADD CONSTRAINT fk_worker_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE worker ADD CONSTRAINT fk_worker_workspaceid FOREIGN KEY (workspaceid) REFERENCES workspace (id);
--------------------------------------------------------------------------------


-- Worker actions --------------------------------------------------------------
CREATE TABLE worker_actions (
  worker_id       VARCHAR(255),
  actions         VARCHAR(255)
);
-- indexes
CREATE INDEX index_worker_actions_actions ON worker_actions (actions);
-- constraints
ALTER TABLE worker_actions ADD CONSTRAINT fk_worker_actions_worker_id FOREIGN KEY (worker_id) REFERENCES worker (id);
--------------------------------------------------------------------------------


-- Stack permissions -----------------------------------------------------------
CREATE TABLE stackpermissions (
  id          VARCHAR(255)         NOT NULL,
  stackid     VARCHAR(255),
  userid      VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_stackpermissions_userid_stackid ON stackpermissions (userid, stackid);
CREATE INDEX index_stackpermissions_stackid ON stackpermissions (stackid);
-- constraints
ALTER TABLE stackpermissions ADD CONSTRAINT fk_stackpermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE stackpermissions ADD CONSTRAINT fk_stackpermissions_stackid FOREIGN KEY (stackid) REFERENCES stack (id);
--------------------------------------------------------------------------------


-- Stack permissions actions ---------------------------------------------------
CREATE TABLE stackpermissions_actions (
  stackpermissions_id     VARCHAR(255),
  actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX index_stackpermissions_actions_actions ON stackpermissions_actions (actions);
-- constraints
ALTER TABLE stackpermissions_actions ADD CONSTRAINT fk_stackpermissions_actions_stackpermissions_id FOREIGN KEY (stackpermissions_id) REFERENCES stackpermissions (id);
--------------------------------------------------------------------------------


-- Recipe permissions ----------------------------------------------------------
CREATE TABLE recipepermissions (
  id          VARCHAR(255)         NOT NULL,
  recipeid    VARCHAR(255),
  userid      VARCHAR(255),

  PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_recipepermissions_userid_recipeid ON recipepermissions (userid, recipeid);
CREATE INDEX index_recipepermissions_recipeid ON recipepermissions (recipeid);
-- constraints
ALTER TABLE recipepermissions ADD CONSTRAINT fk_recipepermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE recipepermissions ADD CONSTRAINT fk_recipepermissions_recipeid FOREIGN KEY (recipeid) REFERENCES recipe (id);
--------------------------------------------------------------------------------


-- Recipe permissions actions --------------------------------------------------
create table recipepermissions_actions (
  recipepermissions_id    varchar(255),
  actions                 VARCHAR(255)
);
-- indexes
create index index_recipepermissions_actions_actions on recipepermissions_actions (actions);
-- constraints
ALTER TABLE recipepermissions_actions ADD CONSTRAINT fk_recipepermissions_actions_recipepermissions_id FOREIGN KEY (recipepermissions_id) REFERENCES recipepermissions (id);
--------------------------------------------------------------------------------
