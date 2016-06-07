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
  constructor($resource, $http) {

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
    this.preferences.$save();
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
}
