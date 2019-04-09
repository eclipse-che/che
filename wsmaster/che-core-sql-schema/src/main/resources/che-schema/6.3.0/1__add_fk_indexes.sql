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

-- Indexes for "reference-side" foreign keys
CREATE INDEX che_index_externalmachine_installers_externalmachine_id ON externalmachine_installers(externalmachine_id);
CREATE INDEX che_index_factory_on_app_closed_action_value_action_entity_id ON che_factory_on_app_closed_action_value(action_entity_id);
CREATE INDEX che_index_factory_on_app_loaded_action_value_action_entity_id ON che_factory_on_app_loaded_action_value(action_entity_id);
CREATE INDEX che_index_factory_on_projects_loaded_action_value_action_entity_id ON che_factory_on_projects_loaded_action_value(action_entity_id);
