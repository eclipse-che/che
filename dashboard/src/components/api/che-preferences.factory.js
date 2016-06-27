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
 * This class is handling the preferences API retrieval
 * @author Yevhenii Voevodin
 */
export class ChePreferences {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $http, $window) {
    this.$window = $window;

    // keep resource
    this.$resource = $resource;

    // http is used for sending data with DELETE method (angular is not sending any data by default with DELETE)
    this.$http = $http;

    // remote call
    this.remotePreferencesAPI = this.$resource('/api/preferences', {}, {
      getPreferences: {method: 'GET', url: '/api/preferences'},
      updatePreferences: {method: 'POST', url: '/api/preferences'}
    });

    // fetch the preferences when we're initialized
    this.fetchPreferences();


    //registry array
    this.registries = [];
  }

  /**
   * Gets the preferences
   * @return preferences
   */
  getPreferences() {
    return this.preferences;
  }

  /**
   * Update the preferences
   * @param properties
   */
  updatePreferences(properties) {
    angular.extend(this.preferences, properties);
    return this.preferences.$save();
  }

  /**
   * Remove preferences properties
   * @param properties (list of keys)
   */
  removePreferences(properties) {
    // delete method doesn't send body when it is defined in $resources
    // that's why direct $http call is used.
    this.$http({
      url: '/api/preferences',
      method: 'DELETE',
      headers: {'Content-Type': 'application/json;charset=utf-8'},
      data: properties}).then(resp => {
        this.fetchPreferences();
    })
  }

  /**
   * Gets the preferences data
   */
  fetchPreferences() {
    let preferences = this.remotePreferencesAPI.getPreferences();
    // if we don't yet have data
    if (!this.preferences) {
      // set preferences for using promise in controllers during first request
      this.preferences = preferences;
    }

    let preferencesPromise = this.preferences.$promise;

    preferencesPromise.then((preferences) => {
      // update preferences data if we have new value
      this.preferences = preferences;
    });

    return preferencesPromise;
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

    if (this.preferences.dockerCredentials) {
      let remoteCredentialsJson = this.$window.atob(this.preferences.dockerCredentials);
      let remoteCredentials = angular.fromJson(remoteCredentialsJson);
      if (remoteCredentials[registryUrl]) {
        delete remoteCredentials[registryUrl];
      }
      angular.extend(credentials, remoteCredentials);
    }

    let credentialsBase64 = this.$window.btoa(angular.toJson(credentials));
    let preferences = {dockerCredentials: credentialsBase64};
    let promise = this.updatePreferences(preferences);

    promise.then((preferences) => {
      this.preferences = preferences;
      this._updateRegistries(preferences);
    });

    return promise;
  }


  /**
   * Remove the registry by its URL
   * @param registryUrl
   * @returns {*} the promise
   */
  removeRegistry(registryUrl) {
    let credentialsJson = this.$window.atob(this.preferences.dockerCredentials);
    let credentials = angular.fromJson(credentialsJson);

    delete credentials[registryUrl];

    let credentialsBase64 = this.$window.btoa(angular.toJson(credentials));
    let preferences = {dockerCredentials: credentialsBase64};

    let promise = this.updatePreferences(preferences);

    promise.then((preferences) => {
      this.preferences = preferences;
      this._updateRegistries(preferences);
    });

    return promise;
  }

  /**
   * Update registry array from preferences
   * @param preferences
   */
  _updateRegistries(preferences) {
    this.registries.length = 0;
    if (preferences.dockerCredentials) {
      let credentialsJson = this.$window.atob(preferences.dockerCredentials);
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
}
