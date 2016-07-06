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
 * This class is handling the profile API retrieval
 * @author Florent Benoit
 */
export class CheProfile {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $http) {
    this.$resource = $resource;

    // http is used for sending data with DELETE method (angular is not sending any data by default with DELETE)
    this.$http = $http;

    // remote call
    this.remoteProfileAPI = this.$resource('/api/profile', {}, {
      getById: {method: 'GET', url: '/api/profile/:userId'},
      setAttributes: {method: 'PUT', url: '/api/profile/attributes'}
    });

    this.profileIdMap = new Map();

    // fetch the profile when we're initialized
    this.fetchProfile();
  }


  /**
   * Gets the profile
   * @return profile
   */
  getProfile() {
    return this.profile;
  }

  /**
   * Gets the full name if it possible
   * @returns {string} full name
   */
  getFullName(attributes) {
    if (!attributes) {
      return '';
    }
    let firstName = attributes.firstName;
    if (!firstName) {
      firstName = '';
    }
    let lastName = attributes.lastName;
    if (!lastName) {
      lastName = '';
    }

    return firstName + ' ' + lastName;
  }

  /**
   * Gets the profile data
   */
  fetchProfile() {
    let profile = this.remoteProfileAPI.get();
    // if we don't yet have data
    if (!this.profile) {
      // set  profile for using promise in controllers during first request
      this.profile = profile;
    }

    let profilePromise = profile.$promise;

    profilePromise.then((profile) => {
      // update profile data if we have new value
      this.profile = profile;
      this.profileIdMap.set(profile.userId, profile);
    });

    return profilePromise;
  }

  /**
   * Set the profile attributes data
   * @param attributes
   * @returns {*} the promise
   */
  setAttributes(attributes) {
    let promise = this.remoteProfileAPI.setAttributes(attributes).$promise;

    return promise;
  }

  /**
   * Fetch the profile from the given userId
   * @param userId the user for which we will call remote api and get promise
   * @returns {*} the promise
   */
  fetchProfileId(userId) {
    let promise = this.remoteProfileAPI.getById({userId: userId}).$promise;
    let parsedResultPromise = promise.then((profile) => {
      this.profileIdMap.set(userId, profile);
    });

    return parsedResultPromise;

  }

  getProfileFromId(userId) {
    return this.profileIdMap.get(userId);
  }


}
