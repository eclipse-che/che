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
    id             VARCHAR(255)    NOT NULL UNIQUE,
    accountid       VARCHAR(255)   NOT NULL,
    devfile_id     BIGINT          NOT NULL UNIQUE,
    meta_generated_name VARCHAR(255) ,
    meta_name VARCHAR(255) ,
    name VARCHAR(255)  NOT NULL ,
    description TEXT ,
    PRIMARY KEY (id)
);
CREATE INDEX index_userdevfile_devfile_id ON userdevfile (devfile_id);
CREATE INDEX index_userdevfile_name ON userdevfile(name);
ALTER TABLE userdevfile ADD CONSTRAINT unq_userdevfile_0 UNIQUE (name, accountid);
ALTER TABLE userdevfile ADD CONSTRAINT fx_userdevfile_accountid FOREIGN KEY (accountid) REFERENCES account (id);
ALTER TABLE userdevfile ADD CONSTRAINT fk_userdevfile_devfile_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
