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

ALTER TABLE usr ADD name VARCHAR(255);

UPDATE usr
SET name = ( SELECT name
             FROM account
             WHERE id = usr.account_id );

ALTER TABLE usr ALTER COLUMN name SET NOT NULL;

CREATE UNIQUE INDEX index_user_name ON usr (name);

ALTER TABLE usr DROP COLUMN account_id;
