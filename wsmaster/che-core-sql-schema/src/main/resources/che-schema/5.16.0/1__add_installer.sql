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

-- Installer ---------------------------------------------------------------------
CREATE TABLE installer (
    id                  VARCHAR(255)         NOT NULL,
    name                VARCHAR(255),
    version             VARCHAR(255)         NOT NULL,
    description         VARCHAR(255),
    script              TEXT,

    PRIMARY KEY (id, version)
);
----------------------------------------------------------------------------------------
-- Installer dependencies ----------------------------------------------------------------
CREATE TABLE installer_dependencies (
    installer_id        VARCHAR(255) NOT NULL,
    installer_version   VARCHAR(255) NOT NULL,
    dependency          VARCHAR(255) NOT NULL
);
--constraints
ALTER TABLE installer_dependencies ADD CONSTRAINT fk_installer_dependencies_id_version FOREIGN KEY (installer_id, installer_version) REFERENCES installer (id, version);
ALTER TABLE installer_dependencies ADD CONSTRAINT unq_installer_dependencies_0 UNIQUE (installer_id, installer_version, dependency);
----------------------------------------------------------------------------------------
-- Installer properties ----------------------------------------------------------------
CREATE TABLE installer_properties (
    installer_id        VARCHAR(255) NOT NULL,
    installer_version   VARCHAR(255) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    value               VARCHAR(255) NOT NULL
);
--constraints
ALTER TABLE installer_properties ADD CONSTRAINT fk_installer_properties_id_version FOREIGN KEY (installer_id, installer_version) REFERENCES installer (id, version);
ALTER TABLE installer_properties ADD CONSTRAINT unq_installer_properties_0 UNIQUE (installer_id, installer_version, name);
----------------------------------------------------------------------------------------
-- Installer properties ----------------------------------------------------------------
CREATE TABLE installer_servers (
    id                  BIGINT NOT NULL,
    installer_id        VARCHAR(255),
    installer_version   VARCHAR(255),
    server_key          VARCHAR(255),
    port                VARCHAR(255),
    protocol            VARCHAR(255),
    path                VARCHAR(255),

    PRIMARY KEY (id)
);
--constraints
ALTER TABLE installer_servers ADD CONSTRAINT fk_installer_servers_id_version FOREIGN KEY (installer_id, installer_version) REFERENCES installer (id, version);
ALTER TABLE installer_servers ADD CONSTRAINT unq_installer_servers_0 UNIQUE (installer_id, installer_version, server_key);
----------------------------------------------------------------------------------------
