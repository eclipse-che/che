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
 * @author Oleksii Orel
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
    this.remotePreferencesAPI = this.$resource('/api/preferences', {}, {});

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
    if (this.preferences && properties) {
      angular.extend(this.preferences, properties);
    } else if (properties) {
      this.preferences = properties;
    } else {
      this.preferences = {};
    }
    return this.remotePreferencesAPI.save(this.preferences).$promise;
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
      data: properties
    }).then(() => {
      this.fetchPreferences();
    })
  }

  /**
   * Gets the preferences data
   */
  fetchPreferences() {
    let promise = this.remotePreferencesAPI.get().$promise;

    promise.then((preferences) => {
      // update preferences data if we have new value
      this._setPreferences(preferences);
    });

    return promise;
  }


  /**
   * Gets the registries
   * @return [*] registries
   */
  getRegistries() {
    return this.registries;
  }

  /**
   * Add a registry
   * @param registryUrl
   * @param userName
   * @param userPassword
   * @returns {*} the promise
   */
  addRegistry(registryUrl, userName, userPassword) {
    let credentials = {};
    credentials[registryUrl] = {
      username: userName,
      password: userPassword
    };

    if (this.preferences && this.preferences.dockerCredentials) {
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
      this._setPreferences(preferences);
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
      this._setPreferences(preferences);
    });

    return promise;
  }

  /**
   * Sets preferences
   * @param preferences
   */
  _setPreferences(preferences) {
    this.preferences = preferences;
    this._updateRegistries();
  }

  /**
   * Update registry array from preferences
   */
  _updateRegistries() {
    this.registries.length = 0;
    if (!this.preferences || !this.preferences.dockerCredentials) {
      return;
    }
    let credentialsJson = this.$window.atob(this.preferences.dockerCredentials);
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
