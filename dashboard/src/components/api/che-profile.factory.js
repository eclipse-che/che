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
  constructor($resource, $http, $window) {
    this.$resource = $resource;
    this.$window = $window;

    // http is used for sending data with DELETE method (angular is not sending any data by default with DELETE)
    this.$http = $http;

    // remote call
    this.remoteProfileAPI = this.$resource('/api/profile', {}, {
      getById: {method: 'GET', url: '/api/profile/:userId'},
      setAttributes: {method: 'POST', url: '/api/profile'}
    });

    // remote call for preferences
    this.remoteProfilePreferencesAPI = this.$resource('/api/profile/prefs');

    this.profileIdMap = new Map();

    // fetch the profile when we're initialized
    this.fetchProfile();

    // fetch the profilePreferences when we're initialized
    this.fetchPreferences();

    //registry array
    this.registries = [];
  }


  /**
   * Gets the profile
   * @return profile
   */
  getProfile() {
    return this.profile;
  }

  /**
   * Gets the preferences
   * @return preferences
   */
  getPreferences() {
    return this.profilePreferences;
  }


  /**
   * Update the preferences
   * @param properties
   * @returns {*} the promise
   */
  updatePreferences(properties) {
    angular.extend(this.profilePreferences, properties);
    return this.profilePreferences.$save();
  }

  /**
   * Gets the registries
   * @return registries
   */
  getRegistries() {
    return this.registries;
  }

  /**
   * Add a registry
   * @param registryUrl
   * @param userName
   * @param userEmail
   * @param userPassword
   * @returns {*} the promise
   */
  addRegistry(registryUrl, userName, userPassword) {
    let credentials = {};
    credentials[registryUrl] = {
      username: userName,
      password: userPassword
    };

    if (this.profilePreferences.dockerCredentials) {
      let remoteCredentialsJson = this.$window.atob(this.profilePreferences.dockerCredentials);
      let remoteCredentials = angular.fromJson(remoteCredentialsJson);
      if (remoteCredentials[registryUrl]) {
        delete remoteCredentials[registryUrl];
      }
      angular.extend(credentials, remoteCredentials);
    }

    let credentialsBase64 = this.$window.btoa(angular.toJson(credentials));
    let preferences = {dockerCredentials: credentialsBase64};
    let promise = this.updatePreferences(preferences);

    promise.then((profilePreferences) => {
      this.profilePreferences = profilePreferences;
      this._updateRegistries(profilePreferences);
    });

    return promise;
  }


  /**
   * Remove the registry by its URL
   * @param registryUrl
   * @returns {*} the promise
   */
  removeRegistry(registryUrl) {
    let credentialsJson = this.$window.atob(this.profilePreferences.dockerCredentials);
    let credentials = angular.fromJson(credentialsJson);

    delete credentials[registryUrl];

    let credentialsBase64 = this.$window.btoa(angular.toJson(credentials));
    let preferences = {dockerCredentials: credentialsBase64};

    let promise = this.updatePreferences(preferences);

    promise.then((profilePreferences) => {
      this.profilePreferences = profilePreferences;
      this._updateRegistries(profilePreferences);
    });

    return promise;
  }

  /**
   * Update registry array from profile preferences
   * @param profilePreferences
   */
  _updateRegistries(profilePreferences) {
    this.registries.length = 0;
    if (profilePreferences.dockerCredentials) {
      let credentialsJson = this.$window.atob(profilePreferences.dockerCredentials);
      let credentials = angular.fromJson(credentialsJson);

      for (var key in credentials) {
        let credential = {
          url: key,
          username: credentials[key].username,
          password: credentials[key].password
        };
        this.registries.push(credential);
      }
    }
  }

  /**
   * Remove preferences properties
   * @param properties (list of keys)
   */
  removePreferences(properties) {
    this.$http({
      url: '/api/profile/prefs',
      method: 'DELETE',
      headers: {'Content-Type': 'application/json;charset=utf-8'},
      data: properties
    });
    this.fetchPreferences();
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
   * Gets the preferences data
   */
  fetchPreferences() {
    let profilePreferences = this.remoteProfilePreferencesAPI.get();
    // if we don't yet have data
    if (!this.profilePreferences) {
      // set profilePreferences for using promise in controllers during first request
      this.profilePreferences = profilePreferences;
    }

    let profilePrefsPromise = this.profilePreferences.$promise;

    profilePrefsPromise.then((profilePreferences) => {
      // update profilePreferences data if we have new value
      this.profilePreferences = profilePreferences;
      this._updateRegistries(profilePreferences);
    });

    return profilePrefsPromise;
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
