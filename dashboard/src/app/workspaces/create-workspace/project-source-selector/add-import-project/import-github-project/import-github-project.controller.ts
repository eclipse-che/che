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

import {CheBranding} from '../../../../../../components/branding/che-branding.factory';
import {ImportGithubProjectService, LoadingState} from './import-github-project.service';
import {ProjectSource} from '../../project-source.enum';
import {IGithubRepository} from './github-repository-interface';
import {AddImportProjectService} from '../add-import-project.service';

/**
 * This class is handling the controller for the GitHub part
 * @author Stéphane Daviet
 * @author Florent Benoit
 * @author Oleksii Kurinnyi
 */
export class ImportGithubProjectController {

  static $inject = ['$q', '$mdDialog', '$location', '$browser', '$scope', 'githubPopup', 'cheBranding', 'githubOrganizationNameResolver',
'importGithubProjectService', 'cheListHelperFactory', 'addImportProjectService', 'keycloakAuth'];

  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Material's dialog service.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Browser service.
   */
  private $browser: ng.IBrowserService;
  /**
   * GitHub authentication popup window.
   */
  private githubPopup: any;
  /**
   * Branding data.
   */
  private cheBranding: CheBranding;
  /**
   * GitHub's organization type resolver service.
   */
  private githubOrganizationNameResolver: any;
  /**
   * Service for adding or importing projects.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Product name.
   */
  private productName: string;
  /**
   * GitHub organization name.
   */
  private resolveOrganizationName: any;
  /**
   * Loading states enum.
   */
  private loadingState: Object;
  /**
   * Import GitHub project service.
   */
  private importGithubProjectService: ImportGithubProjectService;
  /**
   * The GitHub organization.
   */
  private organization: {
    login: string;
    [prop: string]: string;
  };
  /**
   * The helper to manage list of items.
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * Repository filter by name.
   */
  private repositoryFilter: {
    name: string;
  };
  /**
   * Repository filter by organization.
   */
  private organizationFilter: {
    owner: {
      login: string;
    };
  };
  /**
   * The list of GitHub repositories.
   */
  private githubRepositoriesList: Array<IGithubRepository>;
  /**
   * The list of GitHub organization.
   */
  private organizationsList: any;
  /**
   * The list of selected repositories.
   */
  private selectedRepositories: Array<IGithubRepository>;
  /**
   * Keycloak auth service.
   */
  private keycloakAuth: any;

  /**
   * Default constructor that is using resource
   */
  constructor ($q: ng.IQService, $mdDialog: ng.material.IDialogService, $location: ng.ILocationService,
               $browser: any, $scope: ng.IScope, githubPopup: any, cheBranding: CheBranding,
               githubOrganizationNameResolver: any, importGithubProjectService: ImportGithubProjectService,
               cheListHelperFactory: che.widget.ICheListHelperFactory, addImportProjectService: AddImportProjectService, keycloakAuth: any) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.$browser = $browser;
    this.githubPopup = githubPopup;
    this.cheBranding = cheBranding;
    this.githubOrganizationNameResolver = githubOrganizationNameResolver;
    this.resolveOrganizationName = this.githubOrganizationNameResolver.resolve;
    this.addImportProjectService = addImportProjectService;
    this.keycloakAuth = keycloakAuth;

    this.importGithubProjectService = importGithubProjectService;
    this.productName = cheBranding.getName();
    this.loadingState = LoadingState;

    const helperId = 'import-github-project';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.repositoryFilter = {name: ''};
    this.organizationFilter = {
      owner: {
        login: ''
      }
    };

    this.githubRepositoriesList = this.importGithubProjectService.getGithubRepositories();
    this.organizationsList = this.importGithubProjectService.getOrganizations();
    this.cheListHelper.setList(this.githubRepositoriesList, 'clone_url');

    const actionOnPublish = (source: ProjectSource) => {
      this.onAddImportProjectServicePublish(source);
    };
    this.addImportProjectService.subscribe(actionOnPublish);

    $scope.$on('$destroy', () => {
      this.addImportProjectService.unsubscribe(actionOnPublish);
    });

    this.selectedRepositories = this.importGithubProjectService.getSelectedRepositories();
    this.selectedRepositories.forEach((repository: IGithubRepository) => {
      this.cheListHelper.itemsSelectionStatus[repository.clone_url] = true;
    });

    this.importGithubProjectService.setRepositoriesLoadedCallback(() => {
      this.githubRepositoriesList = this.importGithubProjectService.getGithubRepositories();
      this.organizationsList = this.importGithubProjectService.getOrganizations();
      this.cheListHelper.setList(this.githubRepositoriesList, 'clone_url');
    });
  }


  /**
   * Returns current loading state.
   *
   * @return {LoadingState}
   */
  get state(): LoadingState {
    return this.importGithubProjectService.getState();
  }

  /**
   * Callback which is called when repositories are added to the list of ready-to-import projects.
   * Make repositories not selected.
   *
   * @param {ProjectSource} source project's source
   */
  onAddImportProjectServicePublish(source: ProjectSource): void {
    if (source !== ProjectSource.GITHUB) {
      return;
    }

    this.cheListHelper.deselectAllItems();
    this.selectedRepositories = [];

    this.importGithubProjectService.onRepositorySelected(this.selectedRepositories);
  }

  private storeRedirectUri(encodeHash) {
    var redirectUri = location.href;
    if (location.hash && encodeHash) {
      redirectUri = redirectUri.substring(0, location.href.indexOf('#'));
      redirectUri += (redirectUri.indexOf('?') == -1 ? '?' : '&') + 'redirect_fragment=' + encodeURIComponent(location.hash.substring(1));
    }
    window.sessionStorage.setItem('oidcIdeRedirectUrl', redirectUri);
  }

  /**
   * Shows authentication popup window.
   */
  authenticateWithGitHub(): void {
    if (!this.keycloakAuth.isPresent && !this.importGithubProjectService.getIsGitHubOAuthProviderAvailable()) {
      this.$mdDialog.show({
        controller: 'NoGithubOauthDialogController',
        controllerAs: 'noGithubOauthDialogController',
        bindToController: true,
        clickOutsideToClose: true,
        templateUrl: 'app/workspaces/create-workspace/project-source-selector/add-import-project/import-github-project/oauth-dialog/no-github-oauth-dialog.html'
      });

      return;
    }

    if (this.keycloakAuth.isPresent) {
      this.keycloakAuth.keycloak.updateToken(5).success(() => {
        let token = '&token=' + this.keycloakAuth.keycloak.token;
        this.openGithubPopup(token);
      }).error(() => {
        window.sessionStorage.setItem('oidcDashboardRedirectUrl', location.href);
        this.keycloakAuth.keycloak.login();
      });
    } else {
      this.openGithubPopup('');
    }
  }

  /**
   * Opens Github popup.
   *
   * @param {string} token
   */
  openGithubPopup(token: string): void {
    // given URL http://example.com - returns port => 80 (or 443 with https), which causes wrong redirect URL value:
    let port = (this.$location.port() === 80 || this.$location.port() === 443) ? '' : ':' + this.$location.port();
    const redirectUrl = this.$location.protocol() + '://'
      + this.$location.host()
      + port
      + (this.$browser as any).baseHref()
      + 'gitHubCallback.html';
    let link = '/api/oauth/authenticate'
      + '?oauth_provider=github'
      + '&scope=' + ['user', 'repo', 'write:public_key'].join(',')
      + '&userId=' + this.importGithubProjectService.getCurrentUserId()
      + token
      + '&redirect_after_login='
      + redirectUrl;
    this.githubPopup.open(link,
      {
        width: 1020,
        height: 618
      })
      .then( () => {
        return this.importGithubProjectService.getAndStoreRemoteToken();
      }, (rejectionReason: any) => {
        return this.$q.reject(rejectionReason);
      });
  }

  /**
   * Returns organization's type.
   *
   * @param {any} organization the organization
   * @return {string}
   */
  resolveOrganizationType(organization: any): string {
    return organization.name ? 'Your account' : 'Your organization\'s account';
  }

  /**
   * Callback which is called when repository is clicked.
   */
  onRepositorySelected(): void {
    this.selectedRepositories = this.cheListHelper.getSelectedItems() as Array<IGithubRepository>;
    this.importGithubProjectService.onRepositorySelected(this.selectedRepositories);
  }

  /**
   * Callback which is called when search string is changed.
   *
   * @param {string} str the search string
   */
  onSearchChanged(str: string): void {
    this.repositoryFilter.name = str;
    this.cheListHelper.applyFilter('name', this.repositoryFilter);
  };

  /**
   * Callback which is called when organization selected.
   */
  onOrganizationSelect(): void {
    const login = this.organization && this.organization.login ? this.organization.login : '';
    this.organizationFilter.owner.login = login;
    this.cheListHelper.applyFilter('name', this.organizationFilter);
  };

}

