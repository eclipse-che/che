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


export type keycloakUserInfo = {
  email: string;
  family_name: string;
  given_name: string;
  name: string;
  preferred_username: string;
  sub: string;
};

/**
 * This class is handling interactions with Keycloak.
 * @author Oleksii Kurinnyi
 */
export class CheKeycloak {

  static $inject = ['$q', 'keycloakAuth'];

  $q: ng.IQService;
  keycloak: any;
  keycloakConfig: any;

  /**
   * Default constructor that is using resource injection
   */
  constructor($q: ng.IQService, keycloakAuth: any) {
    this.$q = $q;
    this.keycloak = keycloakAuth.keycloak;
    this.keycloakConfig = keycloakAuth.config;
  }

  fetchUserInfo(): ng.IPromise<keycloakUserInfo> {
    const defer = this.$q.defer();

    if (this.keycloak === null) {
      defer.reject('Keycloak is not found on the page.');
      return defer.promise;
    }

    this.keycloak.loadUserInfo().success((userInfo: keycloakUserInfo) => {
      defer.resolve(userInfo);
    }).error((error: any) => {
      defer.reject(`User info fetching failed, error: ${error}`);
    });

    return defer.promise;
  }

  isPresent(): boolean {
    return this.keycloak !== null;
  }

  getProfileUrl(): string {
    return this.keycloak.createAccountUrl();
  }

  logout(): void {
	window.sessionStorage.setItem('oidcDashboardRedirectUrl', location.href);
    this.keycloak.logout({});
  }

}
