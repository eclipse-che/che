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
