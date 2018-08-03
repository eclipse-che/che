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
import {TeamDetailsService} from '../team-details.service';

/**
 * @ngdoc controller
 * @name teams.members:ListTeamOwnersController
 * @description This class is handling the controller for the list of team's owners.
 * @author Ann Shumilova
 */
export class ListTeamOwnersController {

  static $inject = ['cheTeam', 'cheUser', 'chePermissions', 'cheProfile', 'cheNotification', 'lodash', 'teamDetailsService'];

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * User profile API interaction.
   */
  private cheProfile: any;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Team's owners string.
   */
  private owners: string;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Current team's owner (comes from directive's scope).
   */
  private owner: any;

  /**
   * Default constructor that is using resource
   */
  constructor(cheTeam: che.api.ICheTeam, cheUser: any, chePermissions: che.api.IChePermissions, cheProfile: any, cheNotification: any,
              lodash: any, teamDetailsService: TeamDetailsService) {
    this.cheTeam = cheTeam;
    this.cheUser = cheUser;
    this.chePermissions = chePermissions;
    this.cheProfile = cheProfile;
    this.cheNotification = cheNotification;
    this.lodash = lodash;

    this.isLoading = true;
    this.owner = teamDetailsService.getOwner();
    this.processOwner();
  }

  /**
   * Process owner.
   */
  processOwner(): void {
    if (!this.owner) {
      return;
    }
    let profile = this.cheProfile.getProfileById(this.owner.id);
    if (profile) {
      this.formUserItem(profile);
    } else {
      this.cheProfile.fetchProfileById(this.owner.id).then(() => {
        this.formUserItem(this.cheProfile.getProfileById(this.owner.id));
      });
    }
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param user user data
   * @param permissions permissions data
   */
  formUserItem(user: any): void {
    let name = this.cheProfile.getFullName(user.attributes) + ' (' + user.email + ')';
    this.owners = name;
  }
}
