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
import {OrganizationsItemController} from './list-organizations/organizations-item/organizations-item.controller';
import {OrganizationsItem} from './list-organizations/organizations-item/organizations-item.directive';
import {ListOrganizationsController} from './list-organizations/list-organizations.controller';
import {ListOrganizations} from './list-organizations/list-organizations.directive';
import {OrganizationsController} from './organizations.controller';
import {CreateOrganizationController} from './create-organizations/create-organizations.controller';
import {OrganizationDetailsController} from './organization-details/organization-details.controller';
import {ListOrganizationMembersController} from './organization-details/organization-members/list-organization-members.controller';
import {ListOrganizationInviteMembersController} from './organization-details/organization-invite-members/list-organization-invite-members.controller';
import {ListOrganizationInviteMembers} from './organization-details/organization-invite-members/list-organization-invite-members.directive';
import {ListOrganizationMembers} from './organization-details/organization-members/list-organization-members.directive';
import {OrganizationMemberDialogController} from './organization-details/organization-member-dialog/organization-member-dialog.controller';
import {OrganizationsPermissionService} from './organizations-permission.service';
import {OrganizationsConfigService} from './organizations-config.service';
import {OrganizationNotFound} from './organization-details/organization-not-found/organization-not-found.directive';
import {OrganizationSelectMembersDialogController} from './organization-details/organization-select-members-dialog/organization-select-members-dialog.controller';
import {OrganizationMemberItem} from './organization-details/organization-select-members-dialog/organization-member-item/organization-member-item.directive';
import {ListOrganizationWorkspaces} from './organization-details/organization-workspaces/list-organization-workspaces.directive';
import {ListOrganizationWorkspacesController} from './organization-details/organization-workspaces/list-organization-workspaces.controller';

/**
 * The configuration of organizations, defines controllers, directives and routing.
 *
 * @author Oleksii Orel
 */
export class OrganizationsConfig {

  constructor(register: any) {
    register.controller('OrganizationsController', OrganizationsController);

    register.controller('OrganizationDetailsController', OrganizationDetailsController);

    register.controller('OrganizationsItemController', OrganizationsItemController);
    register.directive('organizationsItem', OrganizationsItem);

    register.controller('ListOrganizationMembersController', ListOrganizationMembersController);
    register.directive('listOrganizationMembers', ListOrganizationMembers);

    register.directive('listOrganizationInviteMembers', ListOrganizationInviteMembers);
    register.controller('ListOrganizationInviteMembersController', ListOrganizationInviteMembersController);

    register.controller('OrganizationMemberDialogController', OrganizationMemberDialogController);

    register.controller('CreateOrganizationController', CreateOrganizationController);

    register.controller('ListOrganizationsController', ListOrganizationsController);
    register.directive('listOrganizations', ListOrganizations);

    register.service('organizationsPermissionService', OrganizationsPermissionService);
    register.service('organizationsConfigService', OrganizationsConfigService);

    register.directive('organizationNotFound', OrganizationNotFound);

    register.controller('OrganizationSelectMembersDialogController', OrganizationSelectMembersDialogController);
    register.directive('organizationMemberItem', OrganizationMemberItem);

    register.controller('ListOrganizationWorkspacesController', ListOrganizationWorkspacesController);
    register.directive('listOrganizationWorkspaces', ListOrganizationWorkspaces);

    const organizationDetailsLocationProvider = {
      title: (params: any) => {
        return params.organizationName;
      },
      reloadOnSearch: false,
      templateUrl: 'app/organizations/organization-details/organization-details.html',
      controller: 'OrganizationDetailsController',
      controllerAs: 'organizationDetailsController',
      resolve: {
        initData: ['organizationsConfigService', (organizationConfigService: OrganizationsConfigService) => {
          return organizationConfigService.resolveOrganizationDetailsRoute();
        }]
      }
    };

    const createOrganizationLocationProvider = {
      title: 'New Organization',
      templateUrl: 'app/organizations/create-organizations/create-organizations.html',
      controller: 'CreateOrganizationController',
      controllerAs: 'createOrganizationController',
      resolve: {
        initData: ['organizationsConfigService', (organizationsConfigService: OrganizationsConfigService) => {
          return organizationsConfigService.resolveCreateOrganizationRoute();
        }]
      }
    };

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/organizations', {
        title: 'organizations',
        templateUrl: 'app/organizations/organizations.html',
        controller: 'OrganizationsController',
        controllerAs: 'organizationsController'
      })
        .accessWhen('/admin/create-organization', createOrganizationLocationProvider)
        .accessWhen('/admin/create-organization/:parentQualifiedName*', createOrganizationLocationProvider)
        .accessWhen('/organization/:organizationName*', organizationDetailsLocationProvider);
    }]);
  }
}
