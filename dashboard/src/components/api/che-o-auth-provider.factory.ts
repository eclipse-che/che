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
 * This class is handling the registered oAuth providers.
 * @author Ann Shumilova
 */
export class CheOAuthProvider {

  static $inject = ['$http'];

  private $http: ng.IHttpService;

  private providersByName: Map<string, any>;
  private providersPromise: ng.IPromise<any>;

  /**
   * Default constructor that is using resource
   */
  constructor ($http: ng.IHttpService) {
    this.$http = $http;
    this.providersByName = new Map();
  }

  fetchOAuthProviders(): ng.IPromise<any> {
    if (this.providersPromise) {
      return this.providersPromise;
    }

    let promise = this.$http.get('/api/oauth/');
    this.providersPromise = promise.then((providers: any) => {
      providers.data.forEach((provider: any) => {
        this.providersByName.set(provider.name, provider);
      });
    });

    return this.providersPromise;
  }

  /**
   * Checks whether provider is registered.
   * @param {string} name
   * @returns {boolean}
   */
  isOAuthProviderRegistered(name: string): boolean {
    if (!this.providersByName) {
      return false;
    }

    return !!(this.providersByName.get(name));
  }
}
