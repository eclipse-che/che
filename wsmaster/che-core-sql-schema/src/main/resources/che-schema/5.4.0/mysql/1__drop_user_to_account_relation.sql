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

ALTER TABLE usr ADD name VARCHAR(255);

UPDATE usr
SET name = ( SELECT name
             FROM account
             WHERE id = usr.account_id );

ALTER TABLE usr MODIFY name VARCHAR(255) NOT NULL;

CREATE UNIQUE INDEX index_user_name ON usr (name);

ALTER TABLE usr DROP FOREIGN KEY fk_usr_account_id;

ALTER TABLE usr DROP COLUMN account_id;
