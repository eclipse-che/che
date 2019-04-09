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

-- Machine env ----------------------------------------------
CREATE TABLE externalmachine_env (
    externalmachine_id      BIGINT,
    env_value               VARCHAR(255),
    env_key                 VARCHAR(255)
);
--constraints
ALTER TABLE externalmachine_env ADD CONSTRAINT fk_externalmachine_env_externalmachine_id FOREIGN KEY (externalmachine_id) REFERENCES externalmachine (id);
--indexes
CREATE INDEX index_externalmachine_env_externalmachine_id ON externalmachine_env (externalmachine_id);
--------------------------------------------------------------------------------
