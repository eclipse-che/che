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

interface IUsersResource<T> extends ng.resource.IResourceClass<T> {
  findByID(data: { userId: string }): ng.resource.IResource<T>;
  findByAlias(data: { alias: string }): ng.resource.IResource<T>;
  findByName(data: { name: string }): ng.resource.IResource<T>;
  setPassword(passwordExpression: string): ng.resource.IResource<T>;
  createUser(user: { password: string; name: string; email?: string }): ng.resource.IResource<T>;
  getUsers(data: { maxItems: number; skipCount: number }): ng.resource.IResource<T>;
  removeUserById(data: { userId: string }): ng.resource.IResource<T>;
}

/**
 * This class is handling the user API retrieval
 * @author Oleksii Orel
 */
export class CheUser {

  static $inject = ['$resource', '$q', '$cookies'];

  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private $cookies: ng.cookies.ICookiesService;
  private remoteUserAPI: IUsersResource<any>;
  private logoutAPI: ng.resource.IResourceClass<any>;

  private userIdMap: Map<string, che.IUser>;
  private userAliasMap: Map<string, che.IUser>;
  private userNameMap: Map<string, che.IUser>;
  private usersMap: Map<string, che.IUser>;
  private userPagesMap: Map<number, any>;
  private pageInfo: any;
  /**
   * Current user.
   */
  private user: che.IUser;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, $cookies: ng.cookies.ICookiesService) {
    this.$q = $q;
    this.$resource = $resource;
    this.$cookies = $cookies;

    // remote call
    this.remoteUserAPI = <IUsersResource<any>> this.$resource('/api/user', {}, {
      findByID: {method: 'GET', url: '/api/user/:userId'},
      findByAlias: {method: 'GET', url: '/api/user/find?email=:alias'},
      findByName: {method: 'GET', url: '/api/user/find?name=:name'},
      setPassword: {
        method: 'POST', url: '/api/user/password', isArray: false,
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        }
      },
      createUser: {method: 'POST', url: '/api/user'},
      getUsers: {
        method: 'GET',
        url: '/api/admin/user?maxItems=:maxItems&skipCount=:skipCount',
        isArray: false,
        responseType: 'json',
        transformResponse: (data: any, headersGetter: any) => {
          return this._getPageFromResponse(data, headersGetter('link'));
        }
      },
      removeUserById: {method: 'DELETE', url: '/api/user/:userId'}
    });

    this.logoutAPI = this.$resource('/api/auth/logout', {});

    // users by ID
    this.userIdMap = new Map();

    // users by alias
    this.userAliasMap = new Map();

    // users by name
    this.userNameMap = new Map();

    // all users by ID
    this.usersMap = new Map();

    // page users by relative link
    this.userPagesMap = new Map();

    // pages info
    this.pageInfo = {};

    // current user has to be for sure fetched:
    this.fetchUser();
  }

  /**
   * Create new user
   * @param name - new user name
   * @param email - new user e-mail
   * @param password - new user password
   * @returns {*}
   */
  createUser(name: string, email: string, password: string): ng.IPromise<any> {
    let data: { password: string; name: string; email?: string; } = {password: password, name: name};
    if (email) {
      data.email = email;
    }

    let promise = this.remoteUserAPI.createUser(data).$promise;

    return promise.then((user: che.IUser) => {
      // update users map
      this.usersMap.set(user.id, user);
      return user;
    }, (error: any) => {
      return this.$q.reject(error);
    });
  }

  _getPageFromResponse(data: any, headersLink: string): any {
    let links = new Map();
    if (!headersLink) {
      return {users: data};
    }
    let pattern = new RegExp('<([^>]+?)>.+?rel="([^"]+?)"', 'g');
    let result;
    // look for pattern
    while (result = pattern.exec(headersLink)) {
      // add link
      links.set(result[2], result[1]);
    }
    return {
      users: data,
      links: links
    };
  }

  _getPageParamByLink(pageLink: string): any {
    let lastPageParamMap = new Map();
    let pattern = new RegExp('([_\\w]+)=([\\w]+)', 'g');
    let result;
    while (result = pattern.exec(pageLink)) {
      lastPageParamMap.set(result[1], result[2]);
    }

    let skipCount = lastPageParamMap.get('skipCount');
    let maxItems = lastPageParamMap.get('maxItems');
    if (!maxItems || maxItems === 0) {
      return null;
    }
    return {
      maxItems: maxItems,
      skipCount: skipCount ? skipCount : 0
    };
  }

  _updateCurrentPage(): void {
    let pageData = this.userPagesMap.get(this.pageInfo.currentPageNumber);
    if (!pageData) {
      return;
    }
    this.usersMap.clear();
    if (!pageData.users) {
      return;
    }
    pageData.users.forEach((user: che.IUser) => {
      // add user
      this.usersMap.set(user.id, user);
    });
  }

  _updateCurrentPageUsers(users: Array<any>): void {
    this.usersMap.clear();
    if (!users) {
      return;
    }
    users.forEach((user: che.IUser) => {
      // add user
      this.usersMap.set(user.id, user);
    });
  }

  /**
   * Update user page links by relative direction ('first', 'prev', 'next', 'last')
   */
  _updatePagesData(data: any): any {
    if (!data.links) {
      return;
    }
    let firstPageLink = data.links.get('first');
    if (firstPageLink) {
      let firstPageData: { users?: Array<any>; link?: string; } = {link: firstPageLink};
      if (this.pageInfo.currentPageNumber === 1) {
        firstPageData.users = data.users;
      }
      if (!this.userPagesMap.get(1) || firstPageData.users) {
        this.userPagesMap.set(1, firstPageData);
      }
    }
    let lastPageLink = data.links.get('last');
    if (lastPageLink) {
      let pageParam = this._getPageParamByLink(lastPageLink);
      this.pageInfo.countOfPages = pageParam.skipCount / pageParam.maxItems + 1;
      this.pageInfo.count = pageParam.skipCount;
      let lastPageData: { users?: Array<any>; link?: string; } = {link: lastPageLink};
      if (this.pageInfo.currentPageNumber === this.pageInfo.countOfPages) {
        lastPageData.users = data.users;
      }
      if (!this.userPagesMap.get(this.pageInfo.countOfPages) || lastPageData.users) {
        this.userPagesMap.set(this.pageInfo.countOfPages, lastPageData);
      }
    }
    let prevPageLink = data.links.get('prev');
    let prevPageNumber = this.pageInfo.currentPageNumber - 1;
    if (prevPageNumber > 0 && prevPageLink) {
      let prevPageData = {link: prevPageLink};
      if (!this.userPagesMap.get(prevPageNumber)) {
        this.userPagesMap.set(prevPageNumber, prevPageData);
      }
    }
    let nextPageLink = data.links.get('next');
    let nextPageNumber = this.pageInfo.currentPageNumber + 1;
    if (nextPageNumber) {
      let lastPageData = {link: nextPageLink};
      if (!this.userPagesMap.get(nextPageNumber)) {
        this.userPagesMap.set(nextPageNumber, lastPageData);
      }
    }
  }

  /**
   * Gets the pageInfo
   * @returns {Object}
   */
  getPagesInfo(): any {
    return this.pageInfo;
  }

  /**
   * Ask for loading the users in asynchronous way
   * If there are no changes, it's not updated
   * @param maxItems - the max number of items to return
   * @param skipCount - the number of items to skip
   * @returns {*} the promise
   */
  fetchUsers(maxItems: number, skipCount: number): ng.IPromise<any> {
    let promise = this.remoteUserAPI.getUsers({maxItems: maxItems, skipCount: skipCount}).$promise;

    return promise.then((data: any) => {
      this.pageInfo.currentPageNumber = skipCount / maxItems + 1;
      this._updateCurrentPageUsers(data.users);
      this._updatePagesData(data);
      return this.$q.when();
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.$q.when();
      }
      return this.$q.reject(error);
    });
  }

  /**
   * Ask for loading the users page in asynchronous way
   * If there are no changes, it's not updated
   * @param pageKey {string} - the key of page ('first', 'prev', 'next', 'last'  or '1', '2', '3' ...)
   * @returns {ng.IPromise<any>} the promise
   */
  fetchUsersPage(pageKey: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let pageNumber;
    if ('first' === pageKey) {
      pageNumber = 1;
    } else if ('prev' === pageKey) {
      pageNumber = this.pageInfo.currentPageNumber - 1;
    } else if ('next' === pageKey) {
      pageNumber = this.pageInfo.currentPageNumber + 1;
    } else if ('last' === pageKey) {
      pageNumber = this.pageInfo.countOfPages;
    } else {
      pageNumber = parseInt(pageKey, 10);
    }
    if (pageNumber < 1) {
      pageNumber = 1;
    } else if (pageNumber > this.pageInfo.countOfPages) {
      pageNumber = this.pageInfo.countOfPages;
    }
    let pageData = this.userPagesMap.get(pageNumber);
    if (pageData.link) {
      this.pageInfo.currentPageNumber = pageNumber;
      let promise = this.remoteUserAPI.getUsers(this._getPageParamByLink(pageData.link)).$promise;
      promise.then((data: any) => {
        this._updatePagesData(data);
        pageData.users = data.users;
        this._updateCurrentPage();
        deferred.resolve(data);
      }, (error: any) => {
        if (error && error.status === 304) {
          this._updateCurrentPage();
        }
        deferred.reject(error);
      });
    } else {
      deferred.reject({data: {message: 'Error. No necessary link.'}});
    }
    return deferred.promise;
  }

  /**
   * Gets the users
   * @returns {Map}
   */
  getUsersMap(): Map<string, any> {
    return this.usersMap;
  }

  /**
   * Performs user deleting by the given user ID.
   * @param userId {string} the user id
   * @returns {ng.IPromise<any>} the promise
   */
  deleteUserById(userId: string): ng.IPromise<any> {
    let promise = this.remoteUserAPI.removeUserById({userId: userId}).$promise;

    // check if was OK or not
    promise.then(() => {
      // update users map
      this.usersMap.delete(userId);
    });

    return promise;
  }

  /**
   * Performs current user deletion.
   * @returns {ng.IPromise<any>} the promise
   */
  deleteCurrentUser(): ng.IPromise<any> {
    let userId = this.user.id;
    let promise = this.remoteUserAPI.removeUserById({userId: userId}).$promise;
    return promise;
  }

  /**
   * Performs logout of current user.
   * @returns {ng.IPromise<any>}
   */
  logout(): ng.IPromise<any> {
    let data = {token: this.$cookies['session-access-key']};
    let promise = this.logoutAPI.save(data).$promise;
    return promise;
  }

  /**
   * Gets the user
   * @return user {che.IUser}
   */
  getUser(): che.IUser {
    return this.user;
  }

  /**
   * Fetch the user
   * @returns {ng.IPromise<any>}
   */
  fetchUser(): ng.IPromise<any> {
    let promise = this.remoteUserAPI.get().$promise;
    // check if if was OK or not
    return promise.then((user: che.IUser) => {
      this.user = user;
      return user;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.$q.when(this.user);
      }
      return this.$q.reject(error);
    });
  }

  fetchUserId(userId: string): ng.IPromise<any> {
    let promise = this.remoteUserAPI.findByID({userId: userId}).$promise;

    return promise.then((user: che.IUser) => {
      this.userIdMap.set(userId, user);
      return user;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.userIdMap.get(userId);
      }
      return this.$q.reject(error);
    });
  }

  getUserFromId(userId: string): any {
    return this.userIdMap.get(userId);
  }

  fetchUserByAlias(alias: string): ng.IPromise<any> {
    let promise = this.remoteUserAPI.findByAlias({alias: alias}).$promise;

    return  promise.then((user: che.IUser) => {
      this.userAliasMap.set(alias, user);
      return user;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.userAliasMap.get(alias);
      }
      return this.$q.reject(error);
    });
  }

  getUserByAlias(userAlias: string): any {
    return this.userAliasMap.get(userAlias);
  }

  fetchUserByName(name: string): ng.IPromise<any> {
    let promise = this.remoteUserAPI.findByName({name: name}).$promise;
    let resultPromise = promise.then((user: che.IUser) => {
      this.userNameMap.set(name, user);
      return user;
    }, (error: any) => {
      if (error.status === 304) {
        return this.userNameMap.get(name);
      }
      return this.$q.reject(error);
    });

    return resultPromise;
  }

  getUserByName(name: string): che.IUser {
    return this.userNameMap.get(name);
  }

  setPassword(password: string): ng.IPromise<any> {
    return this.remoteUserAPI.setPassword('password=' + password).$promise;
  }
}
