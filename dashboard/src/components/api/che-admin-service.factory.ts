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
 * This class is handling the all admin services API retrieval.
 * @author Ann Shumilova
 * @author Florent Benoit
 */
export class CheAdminService {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($http) {
    this.$http = $http;
  }

  fetchServices() {
    if (this.servicesPromise) {
      return this.servicesPromise;
    }

    let promise = this.$http.get('/admin/');
    this.servicesPromise = promise.then((response) => {
      this.services = [];

      if (response.data.rootResources) {
        response.data.rootResources.forEach((service) => {
          let path = service.path.charAt(0) === '/' ? service.path.substr(1) : service.path;
          this.services.push(path);
        });
      }
    });

    return this.servicesPromise;
  }

  /**
   * Checks whether service is available.
   * @returns {Boolean|*}
   */
  isServiceAvailable(name) {
    if (!this.services) {
      return false;
    }

    return this.services.indexOf(name) > -1;
  }

  isAdminServiceAvailable() {
    return this.services && this.services.length > 0;
  }
}
