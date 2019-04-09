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

-- Installer ---------------------------------------------------------------------
CREATE TABLE installer (
    internal_id         BIGINT               NOT NULL,
    id                  VARCHAR(255)         NOT NULL,
    name                VARCHAR(255),
    version             VARCHAR(255)         NOT NULL,
    description         VARCHAR(255),
    script              TEXT,

    PRIMARY KEY (internal_id)
);
--constraints
ALTER TABLE installer ADD CONSTRAINT unq_installer_key UNIQUE (id, version);
----------------------------------------------------------------------------------------
-- Installer dependencies ----------------------------------------------------------------
CREATE TABLE installer_dependencies (
    inst_int_id         BIGINT       NOT NULL,
    dependency          VARCHAR(255) NOT NULL
);
--constraints
ALTER TABLE installer_dependencies ADD CONSTRAINT fk_installer_dependencies_inst_int_id FOREIGN KEY (inst_int_id) REFERENCES installer (internal_id);
ALTER TABLE installer_dependencies ADD CONSTRAINT unq_installer_dependency UNIQUE (inst_int_id, dependency);
----------------------------------------------------------------------------------------
-- Installer properties ----------------------------------------------------------------
CREATE TABLE installer_properties (
    inst_int_id         BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    value               VARCHAR(255) NOT NULL
);
--constraints
ALTER TABLE installer_properties ADD CONSTRAINT fk_installer_properties_inst_int_id FOREIGN KEY (inst_int_id) REFERENCES installer (internal_id);
ALTER TABLE installer_properties ADD CONSTRAINT unq_installer_property UNIQUE (inst_int_id, name);
----------------------------------------------------------------------------------------
-- Installer properties ----------------------------------------------------------------
CREATE TABLE installer_servers (
    id                  BIGINT,
    inst_int_id         BIGINT,
    server_key          VARCHAR(255),
    port                VARCHAR(255),
    protocol            VARCHAR(255),
    path                VARCHAR(255),

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE installer_servers ADD CONSTRAINT fk_installer_servers_inst_int_id FOREIGN KEY (inst_int_id) REFERENCES installer (internal_id);
ALTER TABLE installer_servers ADD CONSTRAINT unq_installer_server UNIQUE (inst_int_id, server_key);
----------------------------------------------------------------------------------------
