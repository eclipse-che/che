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

interface IProfileResource<T> extends ng.resource.IResourceClass<T> {
  getById(data: { userId: string }): ng.resource.IResource<T>;
  setAttributes(data: che.IProfileAttributes): ng.resource.IResource<T>;
  setAttributesById(params: { userId: string }, data: che.IProfileAttributes): ng.resource.IResource<T>;
}

/**
 * This class is handling the profile API retrieval
 * @author Florent Benoit
 */
export class CheProfile {

  /**
   * Angular Promise service.
   */
  private $q: ng.IQService;
  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;
  /**
   * Angular Http service.
   */
  private $http: ng.IHttpService;

  private profile: che.IProfile;
  private profileIdMap: Map<string, che.IProfile>;
  private remoteProfileAPI: IProfileResource<che.IProfile>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $resource: ng.resource.IResourceService, $http: ng.IHttpService) {
    this.$q = $q;
    this.$resource = $resource;

    // http is used for sending data with DELETE method (angular is not sending any data by default with DELETE)
    this.$http = $http;

    // remote call
    this.remoteProfileAPI = <IProfileResource<che.IProfile>>this.$resource('/api/profile', {}, {
      getById: {method: 'GET', url: '/api/profile/:userId'},
      setAttributes: {method: 'PUT', url: '/api/profile/attributes'},
      setAttributesById: {method: 'PUT', url: '/api/profile/:userId/attributes'}
    });

    this.profileIdMap = new Map();

    // fetch the profile when we're initialized
    this.fetchProfile();
  }

  /**
   * Gets the full name if it possible
   * @returns {string} full name
   */
  getFullName(attr: che.IProfileAttributes): string {
    if (!angular.isObject(attr)) {
      return '';
    }
    const {firstName, lastName} = attr;
    return `${firstName || ''} ${lastName || ''}`.trim();
  }

  /**
   * Gets the profile
   * @return profile
   */
  getProfile() {
    return this.profile;
  }

  /**
   * Gets the profile data
   * @returns {ng.IPromise<che.IProfile>} the promise
   */
  fetchProfile(): ng.IPromise<che.IProfile> {
    if (this.profile && !this.profile.$resolved) {
      return this.profile;
    }
    let profile = this.remoteProfileAPI.get();
    // if we don't yet have data
    if (!this.profile) {
      // set  profile for using promise in controllers during first request
      this.profile = profile;
    }

    return profile.$promise.then(() => {
      if (!angular.equals(this.profile, profile)) {
        this.profile = profile;
      }
      this.profileIdMap.set(profile.userId, profile);
      return this.profile;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.profile;
      }
      return this.$q.reject(error);
    });
  }

  /**
   * Set the profile attributes data
   * @param attributes {che.IProfileAttributes}
   * @returns {ng.IPromise<any>} the promise
   */
  setAttributes(attributes: che.IProfileAttributes, userId?: string): ng.IPromise<any> {
    if (angular.isUndefined(userId)) {
      return this.remoteProfileAPI.setAttributes(attributes).$promise;
    }
    let promise = this.remoteProfileAPI.setAttributesById({userId: userId}, attributes).$promise;

    return promise.then((profile: che.IProfile) => {
      if (profile && profile.userId) {
        this.profileIdMap.set(profile.userId, profile);
      }
      this.$q.when(profile);
    });
  }

  /**
   * Fetch the profile by the given userId
   * @param userId {string}
   * @returns {ng.IPromise<che.IProfile>}
   */
  fetchProfileById(userId: string): ng.IPromise<che.IProfile> {
    let promise = this.remoteProfileAPI.getById({userId: userId}).$promise;

    return promise.then((profile: che.IProfile) => {
      this.profileIdMap.set(userId, profile);
      return profile;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.profileIdMap.get(userId);
      }
      return this.$q.reject(error);
    });
  }

  getProfileById(userId: string): che.IProfile {
    return this.profileIdMap.get(userId);
  }
}
