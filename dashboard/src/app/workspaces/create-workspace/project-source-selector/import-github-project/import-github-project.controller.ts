/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {CheBranding} from '../../../../../components/branding/che-branding.factory';
import {ImportGithubProjectService, LoadingState} from './import-github-project.service';
import {IProjectSourceSelectorServiceObserver} from '../project-source-selector-service.observer';
import {ProjectSourceSelectorService} from '../project-source-selector.service';
import {ProjectSource} from '../project-source.enum';
import {IGithubRepository} from './github-repository-interface';
import {ActionType} from '../project-source-selector-action-type.enum';

/**
 * This class is handling the controller for the GitHub part
 * @author Stéphane Daviet
 * @author Florent Benoit
 * @author Oleksii Kurinnyi
 */
export class ImportGithubProjectController implements IProjectSourceSelectorServiceObserver {
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
   * Project source selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
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
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($q: ng.IQService, $mdDialog: ng.material.IDialogService, $location: ng.ILocationService, $browser: ng.IBrowserService, $scope: ng.IScope, githubPopup: any, cheBranding: CheBranding, githubOrganizationNameResolver: any, importGithubProjectService: ImportGithubProjectService, cheListHelperFactory: che.widget.ICheListHelperFactory, projectSourceSelectorService: ProjectSourceSelectorService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.$browser = $browser;
    this.githubPopup = githubPopup;
    this.cheBranding = cheBranding;
    this.githubOrganizationNameResolver = githubOrganizationNameResolver;
    this.resolveOrganizationName = this.githubOrganizationNameResolver.resolve;
    this.projectSourceSelectorService = projectSourceSelectorService;

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

    this.selectedRepositories = this.importGithubProjectService.getSelectedRepositories();
    this.projectSourceSelectorService.subscribe(this.onProjectSourceSelectorServicePublish.bind(this));

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
   * @param {ActionType} action the type of action
   * @param {ProjectSource} source the project's source
   */
  onProjectSourceSelectorServicePublish(action: ActionType, source: ProjectSource): void {
    if (action !== ActionType.ADD_PROJECT || source !== ProjectSource.GITHUB) {
      return;
    }

    this.cheListHelper.deselectAllItems();
    this.selectedRepositories = [];

    this.importGithubProjectService.onRepositorySelected(this.selectedRepositories);
  }

  /**
   * Shows authentication popup window.
   */
  authenticateWithGitHub(): void {
    if (!this.importGithubProjectService.getIsGitHubOAuthProviderAvailable()) {
      this.$mdDialog.show({
        controller: 'NoGithubOauthDialogController',
        controllerAs: 'noGithubOauthDialogController',
        bindToController: true,
        clickOutsideToClose: true,
        templateUrl: 'app/workspaces/create-workspace/project-source-selector/import-github-project/oauth-dialog/no-github-oauth-dialog.html'
      });

      return;
    }

    const redirectUrl = this.$location.protocol() + '://'
      + this.$location.host() + ':'
      + this.$location.port()
      + (this.$browser as any).baseHref()
      + 'gitHubCallback.html';
    this.githubPopup.open('/api/oauth/authenticate'
      + '?oauth_provider=github'
      + '&scope=' + ['user', 'repo', 'write:public_key'].join(',')
      + '&userId=' + this.importGithubProjectService.getCurrentUserId()
      + '&redirect_after_login='
      + redirectUrl,
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

