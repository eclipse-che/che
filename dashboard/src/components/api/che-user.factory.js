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

/**
 * This class is handling the user API retrieval
 * @author Florent Benoit
 */
export class CheUser {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $q) {

    // keep resource
    this.$resource = $resource;
    this.$q = $q;

    // remote call
    this.remoteUserAPI = this.$resource('/api/user', {}, {
      findByID: {method: 'GET', url: '/api/user/:userId'},
      findByAlias: {method: 'GET', url: '/api/user/find?alias=:alias'},
      inRole: {method: 'GET', url: '/api/user/inrole?role=:role&scope=:scope&scopeId=:scopeId'},
      setPassword: {
        method: 'POST', url: '/api/user/password', isArray: false,
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        }
      },
      createUser: {method: 'POST', url: '/api/user/create'}
    });

    // users by ID
    this.useridMap = new Map();

    // users by alias
    this.userAliasMap = new Map();

    // user roles
    this.isUserInRoleMap = new Map();

    // fetch the user when we're initialized
    this.userPromise = null;
    this.fetchUser();

    this.isLogged = false;
  }

  /**
   * Create new user
   * @param name - new user name
   * @param email - new user e-mail
   * @param password - new user password
   * @returns {*}
   */
  createUser(name, email, password) {
    let data = {
      password: password,
      name: name
    };

    if (email) {
      data.email = email;
    }

    let promise = this.remoteUserAPI.createUser(data).$promise;

    return promise;
  }

  /**
   * Gets the user ID
   * @return user ID
   */
  getUser() {
    // try to refresh if user is not yet logged in
    if (!this.isLogged) {
      this.fetchUser();
    }
    return this.user;
  }

  /**
   * Gets the user data
   */
  refetchUser() {
    return this.fetchUser(true);
  }

  /**
   * Gets the user data
   */
  fetchUser(ignoreCache) {
    if (!ignoreCache && this.userPromise) {
      return this.userPromise;
    }
    let user = this.remoteUserAPI.get();

    // check admin or not
    let isAdminPromise = this.fetchIsUserInRole('admin', 'system', '');
    let isUserPromise = this.fetchIsUserInRole('user', 'system', '');

    let promise = user.$promise;
    // check if if was OK or not
    let updatePromise = promise.then(() => {
      this.isLogged = true;
    }, () => {
      this.isLogged = false;
    });
    let allPromise = this.$q.all([updatePromise, isUserPromise, isAdminPromise]);
    this.userPromise = allPromise.then(() => {
      this.user = user;
    });

    return this.userPromise;
  }


  fetchUserId(userId) {
    let promise = this.remoteUserAPI.findByID({userId: userId}).$promise;
    let parsedResultPromise = promise.then((user) => {
      this.useridMap.set(userId, user);
    });

    return parsedResultPromise;

  }

  getUserFromId(userId) {
    return this.useridMap.get(userId);
  }

  fetchUserByAlias(alias) {
    let promise = this.remoteUserAPI.findByAlias({alias: alias}).$promise;
    let parsedResultPromise = promise.then((user) => {
      this.useridMap.set(user.id, user);
      this.userAliasMap.set(alias, user);
    });

    return parsedResultPromise;

  }

  getUserByAlias(userAlias) {
    return this.userAliasMap.get(userAlias);
  }

  setPassword(password) {
    let promise = this.remoteUserAPI.setPassword('password=' + password).$promise;

    return promise;
  }

  fetchIsUserInRole(role, scope, scopeId) {
    let promise = this.remoteUserAPI.inRole({role: role, scope: scope, scopeId: scopeId}).$promise;
    let parsedResultPromise = promise.then((userInRole) => {
      this.isUserInRoleMap.set(scope + '/' + role + ':' + scopeId, userInRole);
    }, () => {

    });
    return parsedResultPromise;
  }

  /**
   * Check if useris admin or not by checking the system admin role
   * @returns {*}
   */
  isAdmin() {
    let userInRole = this.isUserInRoleMap.get('system/admin:');
    return userInRole && userInRole.isInRole;
  }

  /**
   * Check if user is user or not by checking the user role
   * @returns {*}
   */
  isUser() {
    let userInRole = this.isUserInRoleMap.get('system/user:');
    return userInRole && userInRole.isInRole;
  }


  /**
   * Forms the string to display from list of roles.
   * @returns {String}
   */
  getDisplayRole(roles) {
    let str = '';

    roles.forEach((role) => {
      let parts = role.split('/');
      str += parts && parts.length > 1 ? parts[1] : role;
      str += ' ';
    });

    return str;
  }

}
