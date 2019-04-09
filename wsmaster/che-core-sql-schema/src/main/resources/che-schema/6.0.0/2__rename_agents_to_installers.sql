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

-- Environment machine installers  -------------------------------------------------
CREATE TABLE externalmachine_installers (
    externalmachine_id  BIGINT,
    installers          VARCHAR(255)
);
-- constraints
ALTER TABLE externalmachine_installers ADD CONSTRAINT fk_externalmachine_installers_externalmachine_id FOREIGN KEY (externalmachine_id) REFERENCES externalmachine (id);

INSERT INTO externalmachine_installers
SELECT * FROM externalmachine_agents;

DROP TABLE externalmachine_agents;
