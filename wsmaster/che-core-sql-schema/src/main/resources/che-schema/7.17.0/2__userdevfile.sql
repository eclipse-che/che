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


-- add userdevfile table
CREATE TABLE userdevfile (
    id          VARCHAR(255)    NOT NULL UNIQUE,
    devfile_id  BIGINT          NOT NULL UNIQUE,
    PRIMARY KEY (id)
);
CREATE INDEX index_userdevfile_devfile_id ON userdevfile (devfile_id);
ALTER TABLE userdevfile ADD CONSTRAINT fk_userdevfile_devfile_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
CREATE INDEX index_devfile_meta_name ON devfile(meta_name);
