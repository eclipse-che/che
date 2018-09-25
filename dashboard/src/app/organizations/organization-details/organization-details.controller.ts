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
import {OrganizationsPermissionService} from '../organizations-permission.service';

enum Tab {Settings, Members, Organization, Workspaces}

/**
 * Controller for a managing organization details.
 *
 * @author Oleksii Orel
 */
export class OrganizationDetailsController {

  static $inject = ['cheResourcesDistribution', 'chePermissions', 'cheUser', '$route', '$location', '$rootScope', '$scope', 'confirmDialogService',
'cheNotification', 'lodash', 'cheOrganization', 'organizationsPermissionService', 'resourcesService', 'initData'];

  tab: Object = Tab;

  /**
   * Organization API interaction.
   */
  private cheOrganization: che.api.ICheOrganization;
  /**
   * Organization resources API interaction.
   */
  private cheResourcesDistribution: che.api.ICheResourcesDistribution;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Route service.
   */
  private $route: ng.route.IRouteService;
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Current organization's name. Comes from route path params.
   */
  private organizationName: string;
  /**
   * Current organization's data (injected by $routeProvider)
   */
  private organization: che.IOrganization;
  /**
   * Parent organization member's list (injected by $routeProvider)
   */
  private parentOrganizationMembers: Array<che.IUser>;
  /**
   * The list of allowed user actions.
   */
  private allowedUserActions: Array<string>;
  /**
   * New organization's name (for renaming widget).
   */
  private newName: string;
  /**
   * Index of the selected tab.
   */
  private selectedTabIndex: number;
  /**
   * Organization limits.
   */
  private limits: any;
  /**
   * Copy of limits before letting to modify, to be able to compare.
   */
  private limitsCopy: any;
  /**
   * Organization total resources.
   */
  private totalResources: any;
  /**
   * Copy of organization total resources before letting to modify, to be able to compare.
   */
  private totalResourcesCopy: any;
  /**
   * Page loading state.
   */
  private isLoading: boolean;

  private organizationForm: ng.IFormController;

  private subOrganizations: Array<any> = [];

  private organizationsPermissionService: OrganizationsPermissionService;

  private resourceLimits: che.resource.ICheResourceLimits;

  private organizationActions: che.resource.ICheOrganizationActions;

  /**
   * Default constructor that is using resource injection
   */
  constructor(cheResourcesDistribution: che.api.ICheResourcesDistribution, chePermissions: che.api.IChePermissions,
              cheUser: any, $route: ng.route.IRouteService, $location: ng.ILocationService, $rootScope: che.IRootScopeService,
              $scope: ng.IScope, confirmDialogService: any, cheNotification: any,
              lodash: any, cheOrganization: che.api.ICheOrganization, organizationsPermissionService: OrganizationsPermissionService, resourcesService: che.service.IResourcesService,
              initData: any) {
    this.cheResourcesDistribution = cheResourcesDistribution;
    this.confirmDialogService = confirmDialogService;
    this.cheOrganization = cheOrganization;
    this.organizationsPermissionService = organizationsPermissionService;
    this.chePermissions = chePermissions;
    this.cheNotification = cheNotification;
    this.cheUser = cheUser;
    this.$location = $location;
    this.$route = $route;
    this.lodash = lodash;
    this.resourceLimits = resourcesService.getResourceLimits();
    this.organizationActions = resourcesService.getOrganizationActions();

    // injected by router
    this.organization = initData.organization as che.IOrganization;
    this.parentOrganizationMembers = initData.parentOrganizationMembers as Array<che.IUser>;

    $rootScope.showIDE = false;

    this.allowedUserActions = [];

    this.updateData();

    this.updateSelectedTab(this.$location.search().tab);
    let deRegistrationFn = $scope.$watch(() => {
      return $location.search().tab;
    }, (tab: string) => {
      if (angular.isDefined(tab)) {
        this.updateSelectedTab(tab);
      }
    }, true);
    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  get SET_PERMISSIONS(): string {
    return this.organizationActions.SET_PERMISSIONS;
  }

  get DELETE(): string {
    return this.organizationActions.DELETE;
  }

  get UPDATE(): string {
    return this.organizationActions.UPDATE;
  }

  /**
   * Fetch sub-organizations.
   */
  fetchSubOrganizations() {
    let manageSubOrganizations = this.isUserAllowedTo(this.organizationActions.MANAGE_SUB_ORGANIZATION);

    if (manageSubOrganizations) {
      this.cheOrganization.fetchSubOrganizationsById(this.organization.id).then((data: any) => {
        this.subOrganizations = data;
      });
    } else {
      this.cheOrganization.fetchOrganizations().then(() => {
        this.subOrganizations = this.lodash.filter(this.cheOrganization.getOrganizations(), (organization: che.IOrganization) => {
          return organization.parent === this.organization.id;
        });
      });
    }
  }

  /**
   * Update data.
   */
  updateData(): void {
    this.organizationName = this.$route.current.params.organizationName;
    if (!this.organization) {
      return;
    }

    this.newName = angular.copy(this.organization.name);

    if (this.isRootOrganization()) {
      this.processTotalResources();
    } else {
      this.processResources();
    }

    this.allowedUserActions = this.processUserPermissions();
    this.fetchSubOrganizations();
  }

  /**
   * Update selected tab index by search part of URL.
   *
   * @param {string} tab
   */
  updateSelectedTab(tab: string): void {
    this.selectedTabIndex = parseInt(this.tab[tab], 10);
  }

  /**
   * Changes search part of URL.
   *
   * @param {number} tabIndex
   */
  onSelectTab(tabIndex?: number): void {
    let param: { tab?: string } = {};
    if (angular.isDefined(tabIndex)) {
      param.tab = Tab[tabIndex];
    }
    if (angular.isUndefined(this.$location.search().tab)) {
      this.$location.replace().search(param);
    } else {
      this.$location.search(param);
    }
  }

  /**
   * Gets sub-organizations for current organization.
   *
   * @returns {Array<any>}
   */
  getSubOrganizations(): Array<any> {
    return this.subOrganizations;
  }

  /**
   * Process permissions to retrieve current user actions.
   *
   * @returns {Array} current user allowed actions
   */
  processUserPermissions(): Array<string> {
    let userId = this.cheUser.getUser().id;
    let permissions = this.chePermissions.getOrganizationPermissions(this.organization.id);
    let userPermissions = this.lodash.find(permissions, (permission: any) => {
      return permission.userId === userId;
    });
    return userPermissions ? userPermissions.actions : [];
  }

  /**
   * Checks whether user is allowed to perform pointed action.
   *
   * @param value action
   * @returns {boolean} <code>true</code> if allowed
   */
  isUserAllowedTo(value: string): boolean {
    if (value === this.organizationActions.UPDATE && this.isPersonalOrganization()) {
      return false;
    }
    return this.allowedUserActions ? this.allowedUserActions.indexOf(value) >= 0 : false;
  }

  /**
   * Checks for personal.
   *
   * @returns {boolean} <code>true</code> if personal
   */
  isPersonalOrganization(): boolean  {
    let user = this.cheUser.getUser();
    return this.organization && user && this.organization.qualifiedName === user.name;
  }

  /**
   * Checks for root.
   *
   * @returns {boolean} <code>true</code> if root
   */
  isRootOrganization(): boolean {
    return this.organization && !this.organization.parent;
  }

  /**
   * Returns whether current user can change organization resource limits.
   *
   * @returns {boolean} <code>true</code> if can change resource limits
   */
  canChangeResourceLimits(): boolean {
    if (this.isRootOrganization()) {
      return this.chePermissions.getUserServices().hasInstallationManagerService;
    }
    return this.organizationsPermissionService.isUserAllowedTo(this.organizationActions.MANAGE_RESOURCES, this.organization.parent);
  }

  /**
   * Check if the name is unique.
   * @param name
   * @returns {boolean}
   */
  isUniqueName(name: string): boolean {
    let currentOrganizationName = this.organization.name;
    let organizations = this.cheOrganization.getOrganizations();
    let account = '';
    let parentId = this.organization.parent;
    if (parentId) {
      let parent = this.cheOrganization.getOrganizationById(parentId);
      if (parent && parent.qualifiedName) {
        account = parent.qualifiedName + '/';
      }
    }
    if (organizations.length && currentOrganizationName !== name) {
      for (let i = 0; i < organizations.length; i++) {
        if (organizations[i].qualifiedName === account + name) {
          return false;
        }
      }
      return true;
    } else {
      return true;
    }
  }

  /**
   * Fetches defined organization's limits (workspace, runtime, RAM caps, etc).
   */
  fetchLimits(): void {
    this.isLoading = true;
    this.cheResourcesDistribution.fetchOrganizationResources(this.organization.id).then(() => {
      this.isLoading = false;
      this.processResources();
    }, (error: any) => {
      this.isLoading = false;
      this.limits = {};
      this.limitsCopy = angular.copy(this.limits);
    });
  }

  /**
   * Process resources limits.
   */
  processResources(): void {
    let ramLimit = this.cheResourcesDistribution.getOrganizationResourceByType(this.organization.id, this.resourceLimits.RAM);
    let workspaceLimit = this.cheResourcesDistribution.getOrganizationResourceByType(this.organization.id, this.resourceLimits.WORKSPACE);
    let runtimeLimit = this.cheResourcesDistribution.getOrganizationResourceByType(this.organization.id, this.resourceLimits.RUNTIME);

    this.limits = {};
    this.limits.workspaceCap = workspaceLimit ? workspaceLimit.amount : undefined;
    this.limits.runtimeCap = runtimeLimit ? runtimeLimit.amount : undefined;
    this.limits.ramCap = ramLimit ? ramLimit.amount / 1024 : undefined;
    this.limitsCopy = angular.copy(this.limits);
  }


  /**
   * Fetches total resources of the organization (workspace, runtime, RAM caps, etc).
   */
  fetchTotalResources(): void {
    this.isLoading = true;
    this.cheResourcesDistribution.fetchTotalOrganizationResources(this.organization.id).then(() => {
      this.isLoading = false;
      this.processTotalResources();
    }, (error: any) => {
      this.isLoading = false;
      this.limits = {};
      this.limitsCopy = angular.copy(this.limits);
    });
  }

  /**
   * Process organization's total resources.
   */
  processTotalResources(): void {
    let ram = this.cheResourcesDistribution.getOrganizationTotalResourceByType(this.organization.id, this.resourceLimits.RAM);
    let workspace = this.cheResourcesDistribution.getOrganizationTotalResourceByType(this.organization.id, this.resourceLimits.WORKSPACE);
    let runtime = this.cheResourcesDistribution.getOrganizationTotalResourceByType(this.organization.id, this.resourceLimits.RUNTIME);

    this.totalResources = {};
    this.totalResources.workspaceCap = (workspace && workspace.amount !== -1) ? workspace.amount : undefined;
    this.totalResources.runtimeCap = (runtime && runtime.amount !== -1) ? runtime.amount : undefined;
    this.totalResources.ramCap = (ram && ram.amount !== -1) ? ram.amount / 1024 : undefined;
    this.totalResourcesCopy = angular.copy(this.totalResources);
  }

  /**
   * Confirms and performs organization's deletion.
   */
  deleteOrganization(): void {
    let promise = this.confirmDialogService.showConfirmDialog('Delete organization',
      'Would you like to delete organization \'' + this.organization.name + '\'?', 'Delete');

    promise.then(() => {
      let promise = this.cheOrganization.deleteOrganization(this.organization.id);
      promise.then(() => {
        this.$location.path('/organizations');

      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Team deletion failed.');
      });
    });
  }

  /**
   * Update organization's details.
   *
   */
  updateOrganizationName(): void {
    if (this.newName && this.organization && this.newName !== this.organization.name) {
      this.organization.name = this.newName;
      this.cheOrganization.updateOrganization(this.organization).then((organization: che.IOrganization) => {
        this.cheOrganization.fetchOrganizations().then(() => {
          this.$location.path('/organization/' + organization.qualifiedName);
        });
      }, (error: any) => {
        this.cheNotification.showError((error.data && error.data.message !== null) ? error.data.message : 'Rename organization failed.');
      });
    }
  }

  /**
   * Update resource limits.
   */
  updateLimits(): void {
    if (!this.organization || !this.limits || angular.equals(this.limits, this.limitsCopy)) {
      return;
    }
    let resources = angular.copy(this.cheResourcesDistribution.getOrganizationResources(this.organization.id));

    let resourcesToRemove = [this.resourceLimits.TIMEOUT];
    if (this.limits.ramCap !== null && this.limits.ramCap !== undefined) {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.RAM, (this.limits.ramCap * 1024).toString());
    } else {
      resourcesToRemove.push(this.resourceLimits.RAM);
    }
    if (this.limits.workspaceCap !== null && this.limits.workspaceCap !== undefined) {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.WORKSPACE, this.limits.workspaceCap);
    } else {
      resourcesToRemove.push(this.resourceLimits.WORKSPACE);
    }
    if (this.limits.runtimeCap !== null && this.limits.runtimeCap !== undefined) {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.RUNTIME, this.limits.runtimeCap);
    } else {
      resourcesToRemove.push(this.resourceLimits.RUNTIME);
    }
    // if the timeout resource will be send in this case - it will set the timeout for the current organization, and the updating timeout of
    // parent organization will not affect the current organization, so to avoid this - remove timeout resource if present:
    this.lodash.remove(resources, (resource: any) => {
      return resourcesToRemove.indexOf(resource.type) >= 0;
    });

    this.isLoading = true;
    this.cheResourcesDistribution.distributeResources(this.organization.id, resources).then(() => {
      this.fetchLimits();
    }, (error: any) => {
      let errorMessage = 'Failed to set update organization CAPs.';
      this.cheNotification.showError((error.data && error.data.message !== null) ? errorMessage + '</br>Reason: ' + error.data.message : errorMessage);
      this.fetchLimits();
    });
  }

  /**
   * Update resource limits.
   */
  updateTotalResources(): void {
    if (!this.organization || !this.totalResources || angular.equals(this.totalResources, this.totalResourcesCopy)) {
      return;
    }
    let resources = angular.copy(this.cheResourcesDistribution.getTotalOrganizationResources(this.organization.id));

    let resourcesToRemove = [this.resourceLimits.TIMEOUT];
    if (this.totalResources.ramCap !== null && this.totalResources.ramCap !== undefined) {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.RAM, (this.totalResources.ramCap * 1024).toString());
    } else {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.RAM, '-1');
    }
    if (this.totalResources.workspaceCap !== null && this.totalResources.workspaceCap !== undefined) {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.WORKSPACE, this.totalResources.workspaceCap);
    } else {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.WORKSPACE, '-1');
    }
    if (this.totalResources.runtimeCap !== null && this.totalResources.runtimeCap !== undefined) {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.RUNTIME, this.totalResources.runtimeCap);
    } else {
      resources = this.cheResourcesDistribution.setOrganizationResourceLimitByType(resources, this.resourceLimits.RUNTIME, '-1');
    }
    // if the timeout resource will be send in this case - it will set the timeout for the current organization, and the updating timeout of
    // parent organization will not affect the current organization, so to avoid this - remove timeout resource if present:
    this.lodash.remove(resources, (resource: any) => {
      return resourcesToRemove.indexOf(resource.type) >= 0;
    });

    this.isLoading = true;

    this.cheResourcesDistribution.updateTotalResources(this.organization.id, resources).then(() => {
      this.fetchTotalResources();
    }, (error: any) => {
      let errorMessage = 'Failed to update organization CAPs.';
      this.cheNotification.showError((error.data && error.data.message !== null) ? errorMessage + '</br>Reason: ' + error.data.message : errorMessage);
      this.fetchTotalResources();
    });
  }

  /**
   * Returns whether save button is disabled.
   *
   * @return {boolean}
   */
  isSaveButtonDisabled(): boolean {
    return !this.organizationForm || this.organizationForm.$invalid;
  }

  /**
   * Returns true if "Save" button should be visible
   *
   * @return {boolean}
   */
  isSaveButtonVisible(): boolean {
    return (this.selectedTabIndex === Tab.Settings && !this.isLoading) && (!angular.equals(this.organization.name, this.newName) ||
      !angular.equals(this.limits, this.limitsCopy) || !angular.equals(this.totalResources, this.totalResourcesCopy));
  }

  /**
   * Returns back button link and title.
   *
   * @returns {any} back button link
   */
  getBackButtonLink(): any {
    if (this.organization && this.organization.parent) {
      let parent = this.organization.qualifiedName.replace(/\/[^\/]+$/, '');
      return {link: '#/organization/' + parent, title: parent};
    } else {
      return {link: '#/organizations', title: 'Organizations'};
    }
  }

  updateOrganization(): void {
    this.updateOrganizationName();
    this.updateLimits();
    this.updateTotalResources();
  }

  cancelChanges(): void {
    this.newName = angular.copy(this.organization.name);
    this.limits = angular.copy(this.limitsCopy);
    this.totalResources = angular.copy(this.totalResourcesCopy);
  }
}
