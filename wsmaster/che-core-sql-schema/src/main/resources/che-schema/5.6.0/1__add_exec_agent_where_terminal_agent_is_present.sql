--
-- Copyright (c) 2012-2017 Codenvy, S.A.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--   Codenvy, S.A. - initial API and implementation
--

INSERT INTO externalmachine_agents
(externalmachine_id, agents)
    SELECT externalmachine_id, 'org.eclipse.che.exec'
    FROM externalmachine_agents
    WHERE agents = 'org.eclipse.che.terminal'
