/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

interface IOrganizationsResource<T> extends ng.resource.IResourceClass<T> {
  findOrganization(data: { name: string }): ng.resource.IResource<T>;
  createOrganization(data: { name: string, parent?: string }): ng.resource.IResource<T>;
  fetchOrganization(data: { id: string }): ng.resource.IResource<T>;
  deleteOrganization(data: { id: string }): ng.resource.IResource<T>;
  updateOrganization(data: { id: string }, organization: che.IOrganization): ng.resource.IResource<T>;
  fetchSubOrganizations(data: { id: string }): ng.resource.IResource<T>;
}

const MAIN_URL = '/api/organization';

/**
 * This class is handling the interactions with Organization management API.
 *
 * @author Oleksii Orel
 */
export class CheOrganization implements che.api.ICheOrganization {
  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private lodash: any;
  /**
   * Factory for PageObjectResource.
   */
  private chePageObject: any;
  /**
   * Current user organization map by organization's id.
   */
  private organizationsByIdMap: Map<string, che.IOrganization> = new Map();
  /**
   * Current user organization map by organization's qualified name.
   */
  private organizationByNameMap: Map<string, che.IOrganization> = new Map();
  /**
   * User organization page map by users's id.
   */
  private userOrganizationPageMap: Map<string, any> = new Map();
  /**
   * Array of current user organizations.
   */
  private currentUserOrganizations: Array<che.IOrganization> = [];
  /**
   * Client for requesting Organization API.
   */
  private remoteOrganizationAPI: IOrganizationsResource<any>;
  /**
   * Organizations map by parent organization's id.
   */
  private subOrganizationsMap: Map<string, Array<che.IOrganization>> = new Map();

  private cheUser: any;

  private pageInfo: che.IPageInfo;

  private organizationRoles: che.resource.ICheOrganizationRoles;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, cheUser: any, lodash: any, chePageObject: any, resourcesService: che.service.IResourcesService) {
    this.chePageObject = chePageObject;
    this.$resource = $resource;
    this.cheUser = cheUser;
    this.lodash = lodash;
    this.$q = $q;
    this.organizationRoles = resourcesService.getOrganizationRoles();

    this.remoteOrganizationAPI = <IOrganizationsResource<any>>$resource(MAIN_URL, {}, {
      fetchOrganization: {method: 'GET', url: MAIN_URL + '/:id'},
      createOrganization: {method: 'POST', url: MAIN_URL},
      deleteOrganization: {method: 'DELETE', url: MAIN_URL + '/:id'},
      updateOrganization: {method: 'POST', url: MAIN_URL + '/:id'},
      fetchSubOrganizations: {method: 'GET', url: MAIN_URL + '/:id/organizations', isArray: true},
      findOrganization: {method: 'GET', url: MAIN_URL + '/find?name=:name'}
    });
  }

  /**
   * Requests organization by it's name.
   *
   * @param name the organization's name
   * @returns {ng.IPromise<any>} result promise
   */
  fetchOrganizationByName(name: string): ng.IPromise<any> {
    let promise = this.remoteOrganizationAPI.findOrganization({'name' : name}).$promise;
    let resultPromise = promise.then((organization: che.IOrganization) => {
      this.organizationByNameMap.set(organization.qualifiedName, organization);
      return organization;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.getOrganizationByName(name);
      }
      return this.$q.reject(error);
    });

    return resultPromise;
  }

  /**
   * Request the list of available organizations with the same parent id.
   *
   * @param id {string} parent organization's id
   * @returns {ng.IPromise<any>}
   */
  fetchSubOrganizationsById(id: string): ng.IPromise<any> {
    let data = {'id': id};
    let promise = this.remoteOrganizationAPI.fetchSubOrganizations(data).$promise;
    let resultPromise = promise.then((organizations: Array<che.IOrganization>) => {
      this.subOrganizationsMap.set(id, organizations);
      return organizations;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.subOrganizationsMap.get(id);
      }
      return this.$q.reject(error);
    });

    return resultPromise;
  }

  /**
   * Request the list of current user organizations for the first page.
   *
   * @returns {ng.IPromise<any>}
   */
  fetchOrganizations(maxItems?: number): ng.IPromise<any> {
    let userDeferred = this.$q.defer();
    let user: che.IUser = this.cheUser.getUser();
    if (angular.isUndefined(user)) {
      this.cheUser.fetchUser().then((user: che.IUser) => {
        userDeferred.resolve(user);
      }, (error: any) => {
        userDeferred.reject(error);
      });
    } else {
      userDeferred.resolve(user);
    }

    return userDeferred.promise.then((user: che.IUser) => {
      let userOrganizationsPageObject = this._getUserOrganizationPage(user.id);
      return this.fetchUserOrganizations(user.id, maxItems).then((organizations: Array<che.IOrganization>) => {
        this.pageInfo = userOrganizationsPageObject.getPagesInfo();
        this._updateCurrentUserOrganizations(organizations);
        return this.$q.when(organizations);
      });
    });
  }

  /**
   * Request the list of current user organizations for a page depends on pageKey('first', 'prev', 'next', 'last').
   * @param pageKey {string}
   * @returns {ng.IPromise<any>}
   */
  fetchOrganizationPageObjects(pageKey?: string): ng.IPromise<any> {
    let user: che.IUser = this.cheUser.getUser();
    if (!user || !user.id) {
      return this.$q.reject({data: {message: 'Error. No user object.'}});
    }
    let userOrganizationsPageObject = this._getUserOrganizationPage(user.id);

    return userOrganizationsPageObject.fetchPageObjects(pageKey).then((organizations: Array<che.IOrganization>) => {
      this._updateCurrentUserOrganizations(organizations);
      this.pageInfo = userOrganizationsPageObject.getPagesInfo();
      return this.$q.when(this.currentUserOrganizations);
    });
  }

  /**
   * Returns the current user page info.
   *
   * @returns {che.IPageInfo}
   */
  getPageInfo(): che.IPageInfo {
    return this.pageInfo;
  }

  /**
   * Request the list of available organizations for an user.
   * @param userId {string}
   * @param maxItems {number}
   * @returns {ng.IPromise<any>}
   */
  fetchUserOrganizations(userId: string, maxItems?: number): ng.IPromise<any> {
    let userOrganizationsPageObject = this._getUserOrganizationPage(userId);

    let promise = userOrganizationsPageObject.fetchObjects(maxItems);

    return promise.then((organizations: Array<che.IOrganization>) => {
      return organizations;
    }, (error: any) => {
        return this.$q.reject(error);
    });
  }

  /**
   * Request the list of user's organizations for a page depends on pageKey('first', 'prev', 'next', 'last').
   * @param userId {string}
   * @param pageKey {string}
   * @returns {ng.IPromise<any>}
   */
  fetchUserOrganizationPageObjects(userId: string, pageKey: string): ng.IPromise<any> {
    let userOrganizationsPageObject = this._getUserOrganizationPage(userId);

    return userOrganizationsPageObject.fetchPageObjects(pageKey);
  }

  /**
   * Returns the array of user's organizations.
   * @param userId {string}
   * @returns {Array<any>} the array of organizations
   */
  getUserOrganizations(userId: string): Array<any> {
    let userOrganizationsPageObject = this._getUserOrganizationPage(userId);

    return userOrganizationsPageObject.getPageObjects();
  }

  /**
   * Returns the user's page info.
   * @param userId {string}
   * @returns {che.IPageInfo}
   */
  getUserOrganizationPageInfo(userId: string): che.IPageInfo {
    let userOrganizationsPageObject = this._getUserOrganizationPage(userId);

    return userOrganizationsPageObject.getPagesInfo();
  }

  /**
   * Returns the user's request data.
   * @param userId {string}
   * @returns {che.IRequestData}
   */
  getUserOrganizationRequestData(userId: string): che.IRequestData {
    let userOrganizationsPageObject = this._getUserOrganizationPage(userId);

    return userOrganizationsPageObject.getRequestDataObject();
  }

  /**
   * Update current user organizations objects
   * @param organizations {Array<che.IOrganization>}
   * @private
   */
  _updateCurrentUserOrganizations(organizations: Array<che.IOrganization>): void {
    this.currentUserOrganizations.length = 0;
    organizations.forEach((organization: che.IOrganization) => {
      this.currentUserOrganizations.push(organization);
      this.organizationByNameMap.set(organization.qualifiedName, organization);
      this.organizationsByIdMap.set(organization.id, organization);
    });
  }

  /**
   * Gets user's page object (create new or get existing one)
   * @param userId
   * @returns {any} - user's page object
   * @private
   */
  _getUserOrganizationPage(userId: string): any {
    let userOrganizationsPageObject: any;
    if (!this.userOrganizationPageMap.has(userId)) {
      userOrganizationsPageObject = this.chePageObject.createPageObjectResource(MAIN_URL, {user: userId}, 'id', this.organizationsByIdMap);
      this.userOrganizationPageMap.set(userId, userOrganizationsPageObject);
      return userOrganizationsPageObject;
    }

    return this.userOrganizationPageMap.get(userId);
  }

  /**
   * Returns the array of current user organizations.
   *
   * @returns {Array<any>} the array of organizations
   */
  getOrganizations(): Array<any> {
    return this.currentUserOrganizations;
  }

  /**
   * Requests organization by it's id.
   *
   * @param id the organization's Id
   * @returns {ng.IPromise<any>} result promise
   */
  fetchOrganizationById(id: string): ng.IPromise<any> {
    let data = {'id': id};
    let promise = this.remoteOrganizationAPI.fetchOrganization(data).$promise;
    return promise.then((organization: che.IOrganization) => {
      this.organizationsByIdMap.set(id, organization);
      return organization;
    }, (error: any) => {
      if (error.status === 304) {
        return this.organizationsByIdMap.get(id);
      }
      return this.$q.reject();
    });
  }

  /**
   * Returns organization by it's id.
   *
   * @param id {string} organization's id
   * @returns {any} organization or <code>null</code> if not found
   */
  getOrganizationById(id: string): che.IOrganization {
    return this.organizationsByIdMap.get(id);
  }

  /**
   * Returns organization by it's name.
   *
   * @param name {string} organization's name
   * @returns {any} organization or <code>null</code> if not found
   */
  getOrganizationByName(name: string): che.IOrganization {
    return this.organizationByNameMap.get(name);
  }

  /**
   * Creates new organization with pointed name.
   *
   * @param name the name of the organization to be created
   * @param parentId {string} the id of the parent organization
   * @returns {ng.IPromise<any>} result promise
   */
  createOrganization(name: string, parentId?: string): ng.IPromise<any> {
    let data: { name: string; parent?: string } = {name: name};
    if (parentId) {
      data.parent = parentId;
    }
    return this.remoteOrganizationAPI.createOrganization(data).$promise;
  }

  /**
   * Delete organization by pointed id.
   *
   * @param id organization's id to be deleted
   * @returns {ng.IPromise<any>} result promise
   */
  deleteOrganization(id: string): ng.IPromise<any> {
    let promise = this.remoteOrganizationAPI.deleteOrganization({'id': id}).$promise;

    return promise.then(() => {
      if (this.organizationsByIdMap.has(id)) {
        const {qualifiedName} = this.organizationsByIdMap.get(id);
        this.organizationsByIdMap.delete(id);
        if (this.organizationByNameMap.has(qualifiedName)) {
          this.organizationByNameMap.delete(qualifiedName);
        }
      }
      return this.$q.when();
    });
  }

  /**
   * Update organization's info.
   *
   * @param organization {che.IOrganization} the organization info to be updated
   * @returns {ng.IPromise<any>} result promise
   */
  updateOrganization(organization: che.IOrganization): ng.IPromise<any> {
    let promise = this.remoteOrganizationAPI.updateOrganization({'id': organization.id}, organization).$promise;

    return promise.then((organization: che.IOrganization) => {
      if (organization) {
        if (this.organizationsByIdMap.has(organization.id)) {
          this.organizationsByIdMap.set(organization.id, organization);
        }
        if (this.organizationByNameMap.has(organization.qualifiedName)) {
          this.organizationByNameMap.set(organization.qualifiedName, organization);
        }
      }
      return organization;
    });
  }

  /**
   * Forms the list of roles based on the list of actions
   *
   * @param actions array of actions
   * @returns {Array<che.IRole>} array of roles
   */
  getRolesFromActions(actions: Array<string>): Array<che.IRole> {
    let roles = [];
    let organizationRoles = this.organizationRoles.getValues();
    organizationRoles.forEach((role: che.IRole) => {
      if (this.lodash.difference(role.actions, actions).length === 0) {
        roles.push(role);
      }
    });

    // avoid roles intake (filter if any role's action is subset of any other):
    roles = this.lodash.filter(roles, (role: che.IRole) => {
      return !this._checkIsSubset(role, roles);
    });

    return roles;
  }

  /**
   * Checks the actions in provided role to be part (subset) of any other role's actions.
   *
   * @param role role to be checked
   * @param roles list of roles
   * @returns {boolean} <code>true</code> if subset
   * @private
   */
  _checkIsSubset(role: any, roles: Array<any>): boolean {
    let isSubset = false;
    for (let i = 0; i < roles.length; i++) {
      let r = roles[i];
      // checks provided role's action is subset of any other role's actions in the roles list:
      if (role.actions.length === this.lodash.intersection(role.actions, r.actions).length && role.actions.length !== r.actions.length) {
        return true;
      }
    }

    return isSubset;
  }

  /**
   * Forms the list actions based on the list of roles.
   *
   * @param {Array<che.IRole>} roles array of roles
   * @returns {Array<string>} actions array
   */
  getActionsFromRoles(roles: Array<che.IRole>): Array<string> {
    let actions = [];
    roles.forEach((role: any) => {
      actions = actions.concat(role.actions);
    });

    return actions;
  }
}
