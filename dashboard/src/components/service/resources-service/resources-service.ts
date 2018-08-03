/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheResourceLimits} from '../../api/che-resource-limits';
import {CheOrganizationActions} from '../../api/che-organization-actions';
import {CheOrganizationRoles} from '../../api/che-organization-roles';
import {CheTeamRoles} from '../../api/che-team-roles';

/**
 * todo
 */
export class ResourcesService implements che.service.IResourcesService {

  getResourceLimits(): che.resource.ICheResourceLimits {
    return CheResourceLimits;
  }

  getOrganizationActions(): che.resource.ICheOrganizationActions {
    return CheOrganizationActions;
  }

  getOrganizationRoles(): che.resource.ICheOrganizationRoles {
    return CheOrganizationRoles;
  }

  getTeamRoles(): che.resource.ICheTeamRoles {
    return CheTeamRoles;
  }

}

