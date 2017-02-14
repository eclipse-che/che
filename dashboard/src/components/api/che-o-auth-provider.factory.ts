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

/**
 * This class is handling the registered oAuth providers.
 * @author Ann Shumilova
 */
export class CheOAuthProvider {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($http) {
    this.$http = $http;
    this.providersByName = new Map();
  }

  fetchOAuthProviders() {
    if (this.providersPromise) {
      return this.providersPromise;
    }

    let promise = this.$http.get('/api/oauth/');
    this.providersPromise = promise.then((providers) => {
      providers.data.forEach((provider) => {
        this.providersByName.set(provider.name, provider);
      });
    });

    return this.providersPromise;
  }

  /**
   * Checks whether provider is registered.
   * @returns {Boolean|*}
   */
  isOAuthProviderRegistered(name) {
    if (!this.providersByName) {
      return false;
    }

    return !!(this.providersByName.get(name));
  }
}
