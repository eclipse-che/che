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
import {CheKeycloak} from '../../components/api/che-keycloak.factory';
import {CheProfile} from '../../components/api/che-profile.factory';
/**
 * This class is handling the controller for the profile page.
 *
 * @author Anna Shumilova
 */
export class ProfileController {

  static $inject = ['cheKeycloak', 'cheProfile', '$window'];

  private profileUrl: string;
  private firstName: string;
  private lastName: string;
  private email: string;
  private userName: string;
  private $window: ng.IWindowService;

  /**
   * Default constructor that is using resource
   */
  constructor(cheKeycloak: CheKeycloak, cheProfile: CheProfile, $window: ng.IWindowService) {
    this.$window = $window;

    this.profileUrl = cheKeycloak.getProfileUrl();
    let profile = cheProfile.getProfile();
    this.firstName = <string>profile.attributes.firstName;
    this.lastName = <string>profile.attributes.lastName;
    this.email = profile.email;
    this.userName = <string>(profile.attributes as any).preferred_username;
  }

  /**
   * Edit profile - redirects to proper page.
   */
  editProfile(): void {
    this.$window.open(this.profileUrl);
  }

  /**
   * Edit profile - redirects to proper page.
   */
  get cannotEdit(): boolean {
    return !this.profileUrl;
  }
}
