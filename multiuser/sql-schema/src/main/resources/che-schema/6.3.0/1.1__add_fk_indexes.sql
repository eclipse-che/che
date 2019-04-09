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
CREATE INDEX che_index_system_permissions_actions_system_permissions_id ON che_system_permissions_actions(system_permissions_id);
CREATE INDEX che_index_stack_permissions_actions_stack_permissions_id ON che_stack_permissions_actions(stack_permissions_id);
CREATE INDEX che_index_worker_actions_worker_id ON che_worker_actions(worker_id);
CREATE INDEX che_index_member_actions_member_id ON che_member_actions(member_id);
CREATE INDEX che_index_organization_account_id ON che_organization(account_id);
CREATE INDEX che_index_organization_distributed_resources_resource_id ON che_organization_distributed_resources_resource(resource_id);
CREATE INDEX che_index_free_resources_limit_resource_resourses_id ON che_free_resources_limit_resource(resources_id);
