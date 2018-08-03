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

/**
 * This class is handling the preferences API retrieval
 * @author Yevhenii Voevodin
 * @author Oleksii Orel
 */
export class ChePreferences {

  static $inject = ['$resource', '$http', '$window'];

  private $window: ng.IWindowService;
  private $resource: ng.resource.IResourceService;
  private $http: ng.IHttpService;
  private remotePreferencesAPI: any;
  private registries: any[];
  private preferences: any;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService,
              $http: ng.IHttpService,
              $window: ng.IWindowService) {
    this.$window = $window;

    // keep resource
    this.$resource = $resource;

    // http is used for sending data with DELETE method (angular is not sending any data by default with DELETE)
    this.$http = $http;

    // remote call
    this.remotePreferencesAPI = this.$resource('/api/preferences', {}, {});

    // registry array
    this.registries = [];
  }

  /**
   * Gets the preferences
   * @return preferences
   */
  getPreferences(): any {
    return this.preferences;
  }

  /**
   * Update the preferences
   * @param {any} properties
   */
  updatePreferences(properties: any): ng.IPromise<any> {
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
  removePreferences(properties: any): void {
    // delete method doesn't send body when it is defined in $resources
    // that's why direct $http call is used.
    this.$http({
      url: '/api/preferences',
      method: 'DELETE',
      headers: {'Content-Type': 'application/json;charset=utf-8'},
      data: properties
    }).then(() => {
      this.fetchPreferences();
    });
  }

  /**
   * Gets the preferences data
   */
  fetchPreferences(): ng.IPromise<any> {
    let promise = this.remotePreferencesAPI.get().$promise;

    promise.then((preferences: any) => {
      // update preferences data if we have new value
      this._setPreferences(preferences);
    });

    return promise;
  }


  /**
   * Gets the registries
   * @return [*] registries
   */
  getRegistries(): any[] {
    return this.registries;
  }

  /**
   * Add a registry
   * @param {string} registryUrl
   * @param {string} userName
   * @param {string} userPassword
   * @returns {*} the promise
   */
  addRegistry(registryUrl: string, userName: string, userPassword: string): ng.IPromise<any> {
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

    promise.then((preferences: any) => {
      this._setPreferences(preferences);
    });

    return promise;
  }

  /**
   * Remove the registry by its URL
   * @param {string} registryUrl
   * @returns {*} the promise
   */
  removeRegistry(registryUrl: string): ng.IPromise<any> {
    let credentialsJson = this.$window.atob(this.preferences.dockerCredentials);
    let credentials = angular.fromJson(credentialsJson);

    delete credentials[registryUrl];

    let credentialsBase64 = this.$window.btoa(angular.toJson(credentials));
    let preferences = {dockerCredentials: credentialsBase64};

    let promise = this.updatePreferences(preferences);

    promise.then((preferences: any) => {
      this._setPreferences(preferences);
    });

    return promise;
  }

  /**
   * Sets preferences
   * @param {any} preferences
   */
  _setPreferences(preferences: any): void {
    this.preferences = preferences;
    this._updateRegistries();
  }

  /**
   * Update registry array from preferences
   */
  _updateRegistries(): void {
    this.registries.length = 0;
    if (!this.preferences || !this.preferences.dockerCredentials) {
      return;
    }
    let credentialsJson = this.$window.atob(this.preferences.dockerCredentials);
    let credentials = angular.fromJson(credentialsJson);

    for (let key in credentials) {
      if (credentials.hasOwnProperty(key)) {
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
