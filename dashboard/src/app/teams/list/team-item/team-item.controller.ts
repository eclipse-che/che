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
 * Controller for a team item.
 *
 * @author Ann Shumilova
 */
export class TeamItemController {

  static $inject = ['$location', 'cheTeam', 'confirmDialogService', 'cheNotification'];

  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Team details (the value is set in directive attributes).
   */
  private team: any;
  /**
   * Callback needed to react on teams updation (the value is set in directive attributes).
   */
  private onUpdate: Function;

  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService, cheTeam: che.api.ICheTeam, confirmDialogService: any, cheNotification: any) {
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.cheTeam = cheTeam;
    this.cheNotification = cheNotification;
  }

  /**
   * Redirect to team details.
   */
  redirectToTeamDetails(tab: string) {
    this.$location.path('/team/' + this.team.qualifiedName).search(!tab ? {} : {tab: tab});
  }

  /**
   * Removes team after confirmation.
   */
  removeTeam(): void {
    this.confirmRemoval().then(() => {
      this.cheTeam.deleteTeam(this.team.id).then(() => {
        this.onUpdate();
      }, (error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete team ' + this.team.name);
      });
    });
  }

  /**
   * Get team display name.
   *
   * @param team
   * @returns {string}
   */
  getTeamDisplayName(team: any): string {
    return this.cheTeam.getTeamDisplayName(team);
  }

  /**
   * Shows dialog to confirm the current team removal.
   *
   * @returns {angular.IPromise<any>}
   */
  confirmRemoval(): ng.IPromise<any> {
    let promise = this.confirmDialogService.showConfirmDialog('Delete team',
      'Would you like to delete team \'' + this.team.name + '\'?', 'Delete');
    return promise;
  }
}

