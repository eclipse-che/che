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
import {CheAPI} from '../../../../../../components/api/che-api.factory';
import {IGithubRepository} from './github-repository-interface';
import {editingProgress, IEditingProgress} from '../../project-source-selector-editing-progress';

export enum LoadingState {
  NO_REPO, IDLE, LOADING, LOADED, LOAD_ERROR
}

/**
 * This class is handling the controller for the GitHub part
 * @author Stéphane Daviet
 * @author Florent Benoit
 * @author Oleksii Kurinnyi
 */
export class ImportGithubProjectService implements IEditingProgress {

  static $inject = ['cheAPI', '$http', '$q', '$filter', 'GitHub', 'gitHubTokenStore'];

  /**
   * API entry point.
   */
  private cheAPI: CheAPI;
  /**
   * HTTP service.
   */
  private $http: ng.IHttpService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Filter service.
   */
  private $filter: ng.IFilterService;
  /**
   * GitHub API
   */
  private GitHub: any;
  /**
   * Token store.
   */
  private gitHubTokenStore: any;
  /**
   * The list of all user's GitHub organizations.
   */
  private organizations: any[];
  /**
   * The list of all user's GitHub repositories.
   */
  private gitHubRepositories: Array<IGithubRepository>;
  /**
   * Loading state enum.
   */
  private state: LoadingState;
  /**
   * <code>true</code> if GitHub OAuth provider is available.
   */
  private isGitHubOAuthProviderAvailable: boolean;
  /**
   * Current user ID.
   */
  private currentUserId: string;
  /**
   * Current token check.
   */
  private currentTokenCheck: any;
  /**
   * The list of selected repositories.
   */
  private selectedRepositories: Array<IGithubRepository>;
  /**
   * Callback which should be called when repositories are loaded.
   */
  private repositoriesLoadedCallback: Function;

  /**
   * Default constructor that is using resource
   */
  constructor (cheAPI: CheAPI, $http: ng.IHttpService, $q: ng.IQService, $filter: ng.IFilterService, GitHub: any, gitHubTokenStore: any) {
    this.cheAPI = cheAPI;
    this.$http = $http;
    this.$q = $q;
    this.$filter = $filter;
    this.GitHub = GitHub;
    this.gitHubTokenStore = gitHubTokenStore;

    this.currentTokenCheck = null;

    this.organizations = [];
    this.gitHubRepositories = [];

    this.selectedRepositories = [];

    this.state = LoadingState.IDLE;
    this.isGitHubOAuthProviderAvailable = false;
  }

  /**
   * Returns projects' adding progress.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    if (this.selectedRepositories.length === 0) {
      return null;
    }

    const number = this.selectedRepositories.length;
    return {
      message: `There ${number === 1 ? 'is' : 'are'} GitHub ${number} ${number === 1 ? 'project' : 'projects'} selected but not added.`,
      number: number
    };
  }

  /**
   * Fetches current user ID.
   *
   * @return {IPromise<any>}
   */
  getOrFetchUserId(): ng.IPromise<any> {
    const defer = this.$q.defer();

    const user = this.cheAPI.getUser().getUser();
    if (user) {
      this.currentUserId = user.id;
      defer.resolve(this.currentUserId);
    } else {
      this.cheAPI.getUser().fetchUser().finally(() => {
        const user = this.cheAPI.getUser().getUser();
        if (user) {
          this.currentUserId = user.id;
        }
        defer.resolve(this.currentUserId);
      });
    }

    return defer.promise;
  }

  /**
   * Returns current user ID.
   *
   * @return {string}
   */
  getCurrentUserId(): string {
    return this.currentUserId;
  }

  /**
   * Fetches GitHub oauth provider.
   *
   * @return {IPromise<any>}
   */
  getOrFetchOAuthProvider(): ng.IPromise<any> {
    const defer = this.$q.defer();
    this.isGitHubOAuthProviderAvailable = this.cheAPI.getOAuthProvider().isOAuthProviderRegistered('github');
    if (this.isGitHubOAuthProviderAvailable) {
      defer.resolve(this.isGitHubOAuthProviderAvailable);
    } else {
      this.cheAPI.getOAuthProvider().fetchOAuthProviders().finally(() => {
        this.isGitHubOAuthProviderAvailable = this.cheAPI.getOAuthProvider().isOAuthProviderRegistered('github');
        defer.resolve(this.isGitHubOAuthProviderAvailable);
      });
    }

    return defer.promise;
  }

  /**
   * Tries to load user's GitHub repositories.
   *
   * @return {IPromise<any>}
   */
  askLoad(): ng.IPromise<any> {
    this.state = LoadingState.LOADING;
    return this.checkTokenValidity().then(() => {
      return this.loadRepositories();
    }).catch(() => {
      this.state = LoadingState.NO_REPO;
    });
  }

  /**
   * Fetches GitHub token.
   *
   * @return {IPromise<any>}
   */
  getAndStoreRemoteToken(): ng.IPromise<any> {
    return this.$http({method: 'GET', url: '/api/oauth/token?oauth_provider=github'}).then( (result: any) => {
      if (!result.data) {
        return false;
      }
      this.gitHubTokenStore.setToken(result.data.token);
      this.$http({method: 'POST', url: '/api/github/ssh/generate'});
      this.askLoad();
      return true;
    });
  }

  /**
   * Checks token validity.
   *
   * @return {any}
   */
  checkTokenValidity(): ng.IPromise<any> {
    if (this.currentTokenCheck) {
      return this.currentTokenCheck;
    }
    this.currentTokenCheck = this.GitHub.user().get( () => {
      this.currentTokenCheck = null;
      return this.$q.defer().resolve(true);
    },  () => {
      this.currentTokenCheck = null;
      return this.$q.defer().reject(false);
    }).$promise;
    return this.currentTokenCheck;
  }

  /**
   * Checks GitHub authentication.
   *
   * @return {IPromise<any>}
   */
  checkGitHubAuthentication(): ng.IPromise<any> {
    return this.checkTokenValidity().then( () => {
      return this.$q.defer().resolve();
    });
  }

  /**
   * Load user's GitHub repositories.
   *
   * @return {IPromise<any>}
   */
  loadRepositories(): ng.IPromise<any> {
    this.organizations.length = 0;
    this.gitHubRepositories.length = 0;

    return this.checkGitHubAuthentication().then( () => {
      const user = this.GitHub.user().get();

      this.organizations.push(user);
      return this.GitHub.organizations().query().$promise.then((organizations: any) => {

        this.organizations = this.organizations.concat(organizations);

        const organizationNames = []; // 'login'
        this.organizations.forEach((organization: any) => {
          if (organization.login) {
            organizationNames.push(organization.login);
          }
        });

        return this.GitHub.userRepositories().query().$promise.then((repositories: Array<IGithubRepository>) => {
          this.gitHubRepositories = this.$filter('filter')(repositories, (repository: IGithubRepository) => {
            return organizationNames.indexOf(repository.owner.login) >= 0;
          });
          if (this.repositoriesLoadedCallback) {
            this.repositoriesLoadedCallback();
          }
          this.state = LoadingState.LOADED;
        });
      });
    }, function () {
      this.state = LoadingState.LOAD_ERROR;
    });
  }

  setRepositoriesLoadedCallback(callback: Function): void {
    this.repositoriesLoadedCallback = callback;
  }

  /**
   * Returns list of user's GitHub organizations.
   *
   * @return {any[]}
   */
  getOrganizations(): any[] {
    return this.organizations;
  }

  /**
   * Returns list of user's GitHub repositories.
   *
   * @return {any[]}
   */
  getGithubRepositories(): Array<IGithubRepository> {
    return this.gitHubRepositories;
  }

  /**
   * Returns current loading start.
   *
   * @return {LoadingState}
   */
  getState(): LoadingState {
    return this.state;
  }

  /**
   * Returns <code>true</code> if GitHub OAuth provider is available.
   *
   * @return {boolean}
   */
  getIsGitHubOAuthProviderAvailable(): boolean {
    return this.cheAPI.getOAuthProvider().isOAuthProviderRegistered('github');
  }

  /**
   * Returns list of selected repositories.
   *
   * @return {any[]}
   */
  getSelectedRepositories(): Array<IGithubRepository> {
    return this.selectedRepositories;
  }

  /**
   * Updates list of selected repositories.
   *
   * @param {any[]} repositories the list of selected repositories.
   */
  onRepositorySelected(repositories: Array<IGithubRepository>): void {
    this.selectedRepositories = repositories;
  }

  /**
   * Builds and returns list of project templates.
   *
   * @return {Array<che.IProjectTemplate>}
   */
  getRepositoriesProps(): Array<che.IProjectTemplate> {
    return this.selectedRepositories.map((repository: IGithubRepository) => {
      const props = {} as che.IProjectTemplate;

      const name = repository.owner.login + '-' + repository.name;
      const path = '/' +  name.replace(/[^\w-_]/g, '_');
      props.name = name;
      props.displayName = name;
      props.description = repository.description || '';
      props.path = path;
      props.category = '';

      props.source = {
        type: 'git',
        location: repository.clone_url
      };

      return props;
    });
  }
}
