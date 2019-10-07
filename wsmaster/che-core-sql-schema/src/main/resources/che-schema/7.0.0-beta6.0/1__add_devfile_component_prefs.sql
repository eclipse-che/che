--
-- Copyright (c) 2012-2019 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

-- component preferences
CREATE TABLE devfile_component_preferences (
    devfile_component_id      BIGINT,
    preference_key            VARCHAR(255),
    preference                VARCHAR(255)
);

-- constraints & indexes
ALTER TABLE devfile_component_preferences ADD CONSTRAINT fk_prefs_component_id FOREIGN KEY (devfile_component_id) REFERENCES devfile_component (id);
CREATE UNIQUE INDEX index_devfile_component_prefs ON devfile_component_preferences (devfile_component_id, preference_key);
