/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

export class CheNavBarCtrl {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($mdSidenav, $scope, $location, $route, cheAPI, $window) {
    this.mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.$window = $window;
    this.links = [{href: '#/create-workspace', name: 'New Workspace'}];

    this.profile = cheAPI.getProfile().getProfile();
    if (this.profile.email) {
      this.email = this.profile.email;
    } else {
      this.profile.$promise.then(() => {
        this.email = this.profile.email ? this.profile.email : 'N/A ';
      }, () => {
        this.email = 'N/A ';
      });
    }

    this.menuItemUrl = {
      dashboard: '#/',
      workspaces: '#/workspaces',
      administration: '#/administration',
      // subsections
      plugins: '#/admin/plugins',
      account: '#/account',
      stacks: '#/stacks'
    };

    // highlight navbar menu item
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path();
      $scope.$broadcast('navbar-selected:set', path);
    });

    cheAPI.cheWorkspace.fetchWorkspaces();
  }

  isImsAvailable() {
    return this.imsArtifactApi.isImsAvailable();
  }

  reload() {
    this.$route.reload();
  }

  /**
   * Toggle the left menu
   */
  toggleLeftMenu() {
    this.mdSidenav('left').toggle();
  }

  getWorkspacesNumber() {
    return this.cheAPI.cheWorkspace.getWorkspaces().length;
  }

  openLinkInNewTab(url) {
    this.$window.open(url, '_blank');
  }
}
