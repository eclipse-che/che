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

-- Indexes for "reference-side" foreign keys
CREATE INDEX che_index_externalmachine_installers_externalmachine_id ON externalmachine_installers(externalmachine_id);
CREATE INDEX che_index_factory_on_app_closed_action_value_action_entity_id ON che_factory_on_app_closed_action_value(action_entity_id);
CREATE INDEX che_index_factory_on_app_loaded_action_value_action_entity_id ON che_factory_on_app_loaded_action_value(action_entity_id);
CREATE INDEX che_index_factory_on_proj_loaded_action_value_action_entity_id ON che_factory_on_projects_loaded_action_value(action_entity_id);
