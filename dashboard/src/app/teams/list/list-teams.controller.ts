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

/**
 * @ngdoc controller
 * @name list.teams:ListTeamsController
 * @description This class is handling the controller for the list of teams
 * @author Ann Shumilova
 */
export class ListTeamsController {

  static $inject = ['cheTeam', 'chePermissions', 'cheResourcesDistribution', 'cheNotification', 'cheTeamEventsManager', 'confirmDialogService', '$scope', '$q', '$location', 'resourcesService'];

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * Team resources API interaction.
   */
  private cheResourcesDistribution: che.api.ICheResourcesDistribution;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * List of teams.
   */
  private teams: Array<any>;
  /**
   * Map of team members.
   */
  private teamMembers: Map<string, number>;
  /**
   * Map of team resources.
   */
  private teamResources: Map<string, any>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Selected status of teams in the list.
   */
  private teamsSelectedStatus: any;
  /**
   * Bulk operation checked state.
   */
  private isBulkChecked: boolean;
  /**
   * No selected workspace state.
   */
  private isNoSelected: boolean;
  /**
   * All selected workspace state.
   */
  private isAllSelected: boolean;
  /**
   * todo
   */
  private resourceLimits: che.resource.ICheResourceLimits;

  /**
   * Default constructor that is using resource
   */
  constructor(cheTeam: che.api.ICheTeam, chePermissions: che.api.IChePermissions, cheResourcesDistribution: che.api.ICheResourcesDistribution,
              cheNotification: any, cheTeamEventsManager: che.api.ICheTeamEventsManager, confirmDialogService: any, $scope: ng.IScope,
              $q: ng.IQService, $location: ng.ILocationService, resourcesService: che.service.IResourcesService) {
    this.cheTeam = cheTeam;
    this.chePermissions = chePermissions;
    this.cheResourcesDistribution = cheResourcesDistribution;
    this.resourceLimits = resourcesService.getResourceLimits();

    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;
    this.$q = $q;
    this.$location = $location;

    this.teams = [];
    this.isLoading = true;

    this.teamsSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;
    this.fetchTeams();


    let refreshHandler = () => {
      this.fetchTeams();
    };

    cheTeamEventsManager.addDeleteHandler(refreshHandler);
    cheTeamEventsManager.addRenameHandler(refreshHandler);

    $scope.$on('$destroy', () => {
      cheTeamEventsManager.removeRenameHandler(refreshHandler);
      cheTeamEventsManager.removeDeleteHandler(refreshHandler);
    });
  }

  /**
   * Fetches the list of teams.
   */
  fetchTeams(): void {
    this.isLoading = true;
    this.cheTeam.fetchTeams().then(() => {
      this.isLoading = false;
      this.processTeams();
    }, (error: any) => {
      this.isLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to retrieve teams.';
      this.cheNotification.showError(message);
    });
  }

  /**
   * Process team - retrieving additional data.
   */
  processTeams(): void {
    this.teams = this.cheTeam.getTeams();
    this.teamMembers = new Map();
    this.teamResources = new Map();

    let promises = [];
    this.teams.forEach((team: any) => {
      let promiseMembers = this.chePermissions.fetchOrganizationPermissions(team.id).then(() => {
        this.teamMembers.set(team.id, this.chePermissions.getOrganizationPermissions(team.id).length);
      }, (error: any) => {
        if (error.status === 304) {
          this.teamMembers.set(team.id, this.chePermissions.getOrganizationPermissions(team.id).length);
        }
      });
      promises.push(promiseMembers);

      let promiseResource = this.cheResourcesDistribution.fetchOrganizationResources(team.id).then(() => {
        this.processResource(team.id);
      }, (error: any) => {
        if (error.status === 304) {
          this.processResource(team.id);
        }
      });

      promises.push(promiseResource);
    });

    this.$q.all(promises).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Process team resources.
   *
   * @param teamId team's id
   */
  processResource(teamId: string): void {
    let ramLimit = this.cheResourcesDistribution.getOrganizationResourceByType(teamId, this.resourceLimits.RAM);
    this.teamResources.set(teamId, ramLimit ? ramLimit.amount : undefined);
  }

  /**
   * Returns the number of team's members.
   *
   * @param teamId team's id
   * @returns {any} number of team members to display
   */
  getMembersCount(teamId: string): any {
    if (this.teamMembers && this.teamMembers.size > 0) {
      return this.teamMembers.get(teamId) || '-';
    }
    return '-';
  }

  /**
   * Returns the RAM limit value.
   *
   * @param teamId team's id
   * @returns {any}
   */
  getRamCap(teamId: string): any {
    if (this.teamResources && this.teamResources.size > 0) {
      let ram = this.teamResources.get(teamId);
      return ram ? (ram / 1024) : null;
    }
    return null;
  }

  /**
   * Returns <code>true</code> if all teams in list are checked.
   *
   * @returns {boolean}
   */
  isAllTeamsSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if all teams in list are not checked.
   *
   * @returns {boolean}
   */
  isNoTeamsSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all teams in list selected.
   */
  selectAllTeams(): void {
    this.teams.forEach((team: any) => {
      this.teamsSelectedStatus[team.id] = true;
    });
  }

  /**
   *  Make all teams in list deselected.
   */
  deselectAllTeams(): void {
    this.teams.forEach((team: any) => {
      this.teamsSelectedStatus[team.id] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllTeams();
      this.isBulkChecked = false;
    } else {
      this.selectAllTeams();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update teams selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.teamsSelectedStatus).forEach((key: string) => {
      if (this.teamsSelectedStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    if (this.isAllSelected) {
      this.isBulkChecked = true;
    }
  }

  /**
   * Redirects to new team creation page.
   */
  createNewTeam(): void {
    this.$location.path('/team/create');
  }

  /**
   * Delete all selected teams.
   */
  removeTeams(): void {
    let teamsSelectedStatusKeys = Object.keys(this.teamsSelectedStatus);
    let checkedTeamsKeys = [];

    if (!teamsSelectedStatusKeys.length) {
      this.cheNotification.showError('No such team.');
      return;
    }

    teamsSelectedStatusKeys.forEach((key: any) => {
      if (this.teamsSelectedStatus[key] === true) {
        checkedTeamsKeys.push(key);
      }
    });

    if (!checkedTeamsKeys.length) {
      this.cheNotification.showError('No such team.');
      return;
    }

    let confirmationPromise = this.showDeleteTeamsConfirmation(checkedTeamsKeys.length);
    confirmationPromise.then(() => {
      this.isLoading = true;
      let promises = [];

      checkedTeamsKeys.forEach((teamId: string) => {
        this.teamsSelectedStatus[teamId] = false;

        let promise = this.cheTeam.deleteTeam(teamId).then(() => {
          //
        }, (error: any) => {
          this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete team ' + teamId + '.');
        });

        promises.push(promise);
      });

      this.$q.all(promises).finally(() => {
        this.fetchTeams();
        this.updateSelectedStatus();
      });
    });
  }

  /**
   * Show confirmation popup before teams deletion.
   *
   * @param numberToDelete number of teams to be deleted
   * @returns {*}
   */
  showDeleteTeamsConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' teams?';
    } else {
      content += 'this selected team?';
    }

    return this.confirmDialogService.showConfirmDialog('Delete teams', content, 'Delete');
  }
}
