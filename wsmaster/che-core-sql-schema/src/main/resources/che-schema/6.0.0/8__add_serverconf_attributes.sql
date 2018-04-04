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

-- ServerConfig attributes ----------------------------------------------
CREATE TABLE serverconf_attributes (
    serverconf_id           BIGINT,
    attributes              VARCHAR(255),
    attributes_key          VARCHAR(255)
);
--constraints
ALTER TABLE serverconf_attributes ADD CONSTRAINT fk_serverconf_attributes_serverconf_id FOREIGN KEY (serverconf_id) REFERENCES serverconf (id);
--indexes
CREATE INDEX index_serverconf_attributes_serverconf_id ON serverconf_attributes (serverconf_id);
-------------------------------------------------------------------------

-- InstallerServerConfig attributes ----------------------------------------------
CREATE TABLE installer_serverconf_attributes (
    serverconf_id           BIGINT,
    attributes              VARCHAR(255),
    attributes_key          VARCHAR(255)
);
--constraints
ALTER TABLE installer_serverconf_attributes ADD CONSTRAINT fk_installer_serverconf_attributes_serverconf_id FOREIGN KEY (serverconf_id) REFERENCES installer_servers (id);
--indexes
CREATE INDEX index_installer_serverconf_attributes_serverconf_id ON installer_serverconf_attributes (serverconf_id);
-------------------------------------------------------------------------
