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
  constructor($mdSidenav, $scope, $location, $route, userDashboardConfig, cheAPI, onBoarding) {
    this.mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.onBoarding = onBoarding;
    this.cheUser = cheAPI.getUser();
    this.links = [{href: '#/create-workspace', name: 'New Workspace'}];

    this.displayLoginItem = userDashboardConfig.developmentMode;

    let promiseAdminService = this.cheAPI.getAdminService().fetchServices();
    promiseAdminService.then(() => {
      this.isAdminServiceAvailable = cheAPI.getAdminService().isAdminServiceAvailable();
      this.isAdminPluginServiceAvailable = cheAPI.getAdminService().isServiceAvailable(cheAPI.getAdminPlugins().getPluginsServicePath());
    });

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
    this.cheUser.fetchUser();

    this.menuItemUrl = {
      dashboard: '#/',
      projects: '#/projects',
      workspaces: '#/workspaces',
      factories: '#/factories',

      // subsection
      plugins: '#/admin/plugins',

      // subsection
      account: '#/account',
      team: '#/team',
      subscriptions: '#/subscriptions',
      billing: '#/billing'
    };

    // clear highlighting of menu item from navbar
    // if route is not part of navbar
    // or restore highlighting otherwise
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path(),
        match = Object.keys(this.menuItemUrl).some(item => this.menuItemUrl[item] === path);
      if (match) {
        $scope.$broadcast('navbar-selected:restore', path);
      }
      else {
        $scope.$broadcast('navbar-selected:clear');
      }
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

  userIsAdmin() {
    return this.cheUser.isAdmin();
  }

  isUser() {
    return this.cheUser.isUser();
  }

  getWorkspacesNumber() {
    return this.cheAPI.cheWorkspace.getWorkspaces().length;
  }
}
