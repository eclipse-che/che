--
--  [2012] - [2017] Codenvy, S.A.
--  All Rights Reserved.
--
-- NOTICE:  All information contained herein is, and remains
-- the property of Codenvy S.A. and its suppliers,
-- if any.  The intellectual and technical concepts contained
-- herein are proprietary to Codenvy S.A.
-- and its suppliers and may be covered by U.S. and Foreign Patents,
-- patents in process, and are protected by trade secret or copyright law.
-- Dissemination of this information or reproduction of this material
-- is strictly forbidden unless prior written permission is obtained
-- from Codenvy S.A..
--

CREATE INDEX index_command_attributes_commandid ON command_attributes (command_id);
CREATE INDEX index_command_commandsid ON command (commands_id);

CREATE INDEX index_factory_action_properties_entityid ON che_factory_action_properties (action_entity_id);
CREATE INDEX index_factory_buttonid ON che_factory (button_id);
CREATE INDEX index_factory_ideid ON che_factory (ide_id);
CREATE INDEX index_factory_userid ON che_factory (user_id);
CREATE INDEX index_factory_workspaceid ON che_factory (workspace_id);
CREATE INDEX index_factory_images_factoryid ON che_factory_image (factory_id);
CREATE INDEX index_ide_onappclosedid ON che_factory_ide (on_app_closed_id);
CREATE INDEX index_ide_onapploadedid ON che_factory_ide (on_app_loaded_id);
CREATE INDEX index_ide_onprojectsloadedid ON che_factory_ide (on_projects_loaded_id);

CREATE INDEX index_environment_environmentsid ON environment (environments_id);
CREATE INDEX index_externalmachine_agents_externalmachineid ON externalmachine_agents (externalmachine_id);
CREATE INDEX index_externalmachine_attributes_externalmachineid ON externalmachine_attributes (externalmachine_id);
CREATE INDEX index_externalmachine_machinesid ON externalmachine (machines_id);

CREATE INDEX index_preference_preferences_userid ON preference_preferences (preference_userid);
CREATE INDEX index_profile_attributes_userid ON profile_attributes (user_id);
CREATE INDEX index_projectattribute_dbattributesid ON projectattribute (dbattributes_id);
CREATE INDEX index_projectattribute_values_projectattributeid ON projectattribute_values (projectattribute_id);
CREATE INDEX index_projectconfig_mixins_projectconfigid ON projectconfig_mixins (projectconfig_id);
CREATE INDEX index_projectconfig_projectsid ON projectconfig (projects_id);
CREATE INDEX index_projectconfig_sourceid ON projectconfig (source_id);
CREATE INDEX index_recipe_tags_recipeid ON recipe_tags (recipe_id);

CREATE INDEX index_serverconf_properties_serverconfid ON serverconf_properties (serverconf_id);
CREATE INDEX index_serverconf_serversid ON serverconf (servers_id);
CREATE INDEX index_snapshot_workspaceid ON snapshot (workspaceid);
CREATE INDEX index_sourcestorage_parameters_sourcestorageid ON sourcestorage_parameters (sourcestorage_id);
CREATE INDEX index_stack_components_stackid ON stack_components (stack_id);
CREATE INDEX index_stack_tags_stackid ON stack_tags (stack_id);
CREATE INDEX index_stack_workspaceconfigid ON stack (workspaceconfig_id);

CREATE INDEX index_user_aliases_userid ON user_aliases (user_id);
CREATE INDEX index_workspace_configid ON workspace (config_id);
CREATE INDEX index_workspace_attributes_workspaceid ON workspace_attributes (workspace_id);
