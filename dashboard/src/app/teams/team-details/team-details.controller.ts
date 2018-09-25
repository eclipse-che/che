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
import {TeamDetailsService} from './team-details.service';

enum Tab {Settings, Members, Workspaces}

/**
 * Controller for a managing team details.
 *
 * @author Ann Shumilova
 */
export class TeamDetailsController {

  static $inject = ['cheTeam', 'cheResourcesDistribution', 'chePermissions', 'cheUser', '$route', '$location', '$rootScope', '$scope', 'confirmDialogService',
'cheTeamEventsManager', 'cheNotification', 'lodash', 'teamDetailsService', 'resourcesService'];

tab: Object = Tab;

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Team events manager.
   */
  private cheTeamEventsManager: che.api.ICheTeamEventsManager;
  /**
   * Team resources API interaction.
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
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Current team's name. Comes from route path params.
   */
  private teamName: string;
  /**
   * Current team's data.
   */
  private team: any;
  /**
   * Current team's owner.
   */
  private owner: any;
  /**
   * The list of allowed user actions.
   */
  private allowedUserActions: Array<string>;
  /**
   * New team's name (for renaming widget).
   */
  private newName: string;
  /**
   * Index of the selected tab.
   */
  private selectedTabIndex: number;
  /**
   * Team limits.
   */
  private limits: any;
  /**
   * Copy of limits before letting to modify, to be able to compare.
   */
  private limitsCopy: any;
  /**
   * Page loading state.
   */
  private isLoading: boolean;

  private teamForm: ng.IFormController;

  private hasTeamAccess: boolean;

  private resourceLimits: che.resource.ICheResourceLimits;
  /**
   * Default constructor that is using resource injection
   */
  constructor(cheTeam: che.api.ICheTeam, cheResourcesDistribution: che.api.ICheResourcesDistribution, chePermissions: che.api.IChePermissions,
              cheUser: any, $route: ng.route.IRouteService, $location: ng.ILocationService, $rootScope: che.IRootScopeService,
              $scope: ng.IScope, confirmDialogService: any, cheTeamEventsManager: che.api.ICheTeamEventsManager, cheNotification: any,
              lodash: any, teamDetailsService: TeamDetailsService, resourcesService: che.service.IResourcesService) {
    this.cheTeam = cheTeam;
    this.cheResourcesDistribution = cheResourcesDistribution;
    this.chePermissions = chePermissions;
    this.cheTeamEventsManager = cheTeamEventsManager;
    this.cheUser = cheUser;
    this.teamName = $route.current.params.teamName;
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.cheNotification = cheNotification;
    this.lodash = lodash;
    this.resourceLimits = resourcesService.getResourceLimits();

    $rootScope.showIDE = false;

    this.allowedUserActions = [];

    let deleteHandler = (info: any) => {
      if (this.team && (this.team.id === info.organization.id)) {
        this.$location.path('/workspaces');
      }
    };
    this.cheTeamEventsManager.addDeleteHandler(deleteHandler);

    let renameHandler = (info: any) => {
      if (this.team && (this.team.id === info.organization.id)) {
        this.$location.path('/team/' + info.organization.qualifiedName);
      }
    };
    this.cheTeamEventsManager.addRenameHandler(renameHandler);

    this.updateSelectedTab(this.$location.search().tab);
    let deRegistrationFn = $scope.$watch(() => {
      return $location.search().tab;
    }, (tab: string) => {
      if (angular.isDefined(tab)) {
        this.updateSelectedTab(tab);
      }
    }, true);
    $scope.$on('$destroy', () => {
      this.cheTeamEventsManager.removeRenameHandler(renameHandler);
      this.cheTeamEventsManager.removeDeleteHandler(deleteHandler);
      deRegistrationFn();
    });

    this.isLoading = true;
    this.hasTeamAccess = true;
    this.team = teamDetailsService.getTeam();
    this.owner = teamDetailsService.getOwner();

    if (this.team) {
      this.newName = angular.copy(this.team.name);
      if (this.owner) {
        this.fetchUserPermissions();
      } else {
        teamDetailsService.fetchOwnerByTeamName(this.teamName).then((owner: any) => {
          this.owner = owner;
        }, (error: any) => {
          this.isLoading = false;
          cheNotification.showError(error && error.data && error.data.message !== null ? error.data.message : 'Failed to find team owner.');
        }).finally(() => {
          this.fetchUserPermissions();
        });
      }
    }
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
   * Fetches permission of user in current team.
   */
  fetchUserPermissions(): void {
    this.chePermissions.fetchOrganizationPermissions(this.team.id).then(() => {
      this.allowedUserActions = this.processUserPermissions();
      this.hasTeamAccess = this.allowedUserActions.length > 0;
      this.fetchLimits();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.allowedUserActions = this.processUserPermissions();
        this.fetchLimits();
      } else if (error.status === 403) {
        this.allowedUserActions = [];
        this.hasTeamAccess = false;
      }
      this.isLoading = false;
    });
  }

  /**
   * Process permissions to retrieve current user actions.
   *
   * @returns {Array} current user allowed actions
   */
  processUserPermissions(): Array<string> {
    let userId = this.cheUser.getUser().id;
    let permissions = this.chePermissions.getOrganizationPermissions(this.team.id);
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
    return this.allowedUserActions ? this.allowedUserActions.indexOf(value) >= 0 : false;
  }

  /**
   * Returns whether current user can change team resource limits.
   *
   * @returns {boolean} <code>true</code> if can change resource limits
   */
  canChangeResourceLimits(): boolean {
    return (this.cheTeam.getPersonalAccount() && this.team) ? this.cheTeam.getPersonalAccount().id === this.team.parent : false;
  }

  /**
   * Returns whether current user can leave team (owner of the team is not allowed to leave it).
   *
   * @returns {boolean} <code>true</code> if can leave team
   */
  canLeaveTeam(): boolean {
    return (this.cheTeam.getPersonalAccount() && this.team) ? this.cheTeam.getPersonalAccount().id !== this.team.parent : false;
  }

  /**
   * Fetches defined team's limits (workspace, runtime, RAM caps, etc).
   */
  fetchLimits(): void {
    this.isLoading = true;
    this.cheResourcesDistribution.fetchOrganizationResources(this.team.id).then(() => {
      this.isLoading = false;
      this.processResources();
    }, (error: any) => {
      this.isLoading = false;
      if (!error) {
        return;
      }
      if (error.status === 304) {
        this.processResources();
      } else if (error.status === 404) {
        this.limits = {};
        this.limitsCopy = angular.copy(this.limits);
      }
    });
  }

  /**
   * Process resources limits.
   */
  processResources(): void {
    let ramLimit = this.cheResourcesDistribution.getOrganizationResourceByType(this.team.id, this.resourceLimits.RAM);
    let workspaceLimit = this.cheResourcesDistribution.getOrganizationResourceByType(this.team.id, this.resourceLimits.WORKSPACE);
    let runtimeLimit = this.cheResourcesDistribution.getOrganizationResourceByType(this.team.id, this.resourceLimits.RUNTIME);

    this.limits = {};
    this.limits.workspaceCap = workspaceLimit ? workspaceLimit.amount : undefined;
    this.limits.runtimeCap = runtimeLimit ? runtimeLimit.amount : undefined;
    this.limits.ramCap = ramLimit ? ramLimit.amount / 1024 : undefined;
    this.limitsCopy = angular.copy(this.limits);
  }

  /**
   * Confirms and performs team's deletion.
   *
   * @param event
   */
  deleteTeam(event: MouseEvent): void {
    let promise = this.confirmDialogService.showConfirmDialog('Delete team',
      'Would you like to delete team \'' + this.team.name + '\'?', 'Delete');

    promise.then(() => {
      let promise = this.cheTeam.deleteTeam(this.team.id);
      promise.then(() => {
        this.$location.path('/');
        this.cheTeam.fetchTeams();
      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Team deletion failed.');
      });
    });
  }

  /**
   * Confirms and performs removing user from current team.
   *
   */
  leaveTeam(): void {
    let promise = this.confirmDialogService.showConfirmDialog('Leave team',
      'Would you like to leave team \'' + this.team.name + '\'?', 'Leave');

    promise.then(() => {
      let promise = this.chePermissions.removeOrganizationPermissions(this.team.id, this.cheUser.getUser().id);
      promise.then(() => {
        this.$location.path('/');
        this.cheTeam.fetchTeams();
      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Leave team failed.');
      });
    });
  }

  /**
   * Update team's details.
   *
   */
  updateTeamName(): void {
    if (this.newName && this.team && this.newName !== this.team.name) {
      this.team.name = this.newName;
      this.cheTeam.updateTeam(this.team).then((team: any) => {
        this.cheTeam.fetchTeams().then(() => {
          this.$location.path('/team/' + team.qualifiedName);
        });
      }, (error: any) => {
        this.cheNotification.showError((error.data && error.data.message !== null) ? error.data.message : 'Rename team failed.');
      });
    }
  }

  /**
   * Update resource limits.
   *
   */
  updateLimits(): void {
    if (!this.team || !this.limits || angular.equals(this.limits, this.limitsCopy)) {
      return;
    }

    let resources = angular.copy(this.cheResourcesDistribution.getOrganizationResources(this.team.id));

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

    // if the timeout resource will be send in this case - it will set the timeout for the current team, and the updating timeout of
    // parent team will not affect the current team, so to avoid this - remove timeout resource if present:
    this.lodash.remove(resources, (resource: any) => {
      return resourcesToRemove.indexOf(resource.type) >= 0;
    });


    this.isLoading = true;
    this.cheResourcesDistribution.distributeResources(this.team.id, resources).then(() => {
      this.fetchLimits();
    }, (error: any) => {
      let errorMessage = 'Failed to set update team CAPs.';
      this.cheNotification.showError((error.data && error.data.message !== null) ? errorMessage + '</br>Reason: ' + error.data.message : errorMessage);
      this.fetchLimits();
    });
  }

  /**
   * Returns whether save button is disabled.
   *
   * @return {boolean}
   */
  isSaveButtonDisabled(): boolean {
    return this.teamForm && this.teamForm.$invalid;
  }

  /**
   * Returns true if "Save" button should be visible
   *
   * @return {boolean}
   */
  isSaveButtonVisible(): boolean {
    return (this.selectedTabIndex === Tab.Settings && !this.isLoading) && (!angular.equals(this.team.name, this.newName) ||
      !angular.equals(this.limits, this.limitsCopy));
  }

  updateTeam(): void {
    this.updateTeamName();
    this.updateLimits();
  }

  cancelChanges(): void {
    this.newName = angular.copy(this.team.name);
    this.limits = angular.copy(this.limitsCopy);
  }
}
