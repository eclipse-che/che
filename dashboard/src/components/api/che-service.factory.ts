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
 * This class is handling the all services API retrieval.
 * @author Ann Shumilova
 */
export class CheService {
  /**
   * Service for performing HTTP requests.
   */
  private $http: ng.IHttpService;

  private servicesPromise: ng.IPromise;
  /**
   * The list of available services.
   */
  private services: Array<string>;
  /**
   * Information about services.
   */
  private servicesInfo: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($http: ng.IHttpService) {
    this.$http = $http;
  }

  /**
   * Fetches all available services.
   *
   * @returns {ng.IPromise}
   */
  fetchServices(): ng.IPromise {
    if (this.servicesPromise) {
      return this.servicesPromise;
    }

    let promise = this.$http.get('/api/');
    this.servicesPromise = promise.then((response: any) => {
      this.services = [];
      response.data.rootResources.forEach((service: any) => {
        let path = service.path.charAt(0) === '/' ? service.path.substr(1) : service.path;
        this.services.push(path);
      });
    });

    return this.servicesPromise;
  }

  /**
   * Checks whether service is available.
   * @param name service name
   * @returns {Boolean|*}
   */
  isServiceAvailable(name: string): boolean {
    if (!this.services) {
      return false;
    }

    return this.services.indexOf(name) > -1;
  }

  /**
   * Fetches the services info.
   *
   * @returns {IHttpPromise<T>}
   */
  fetchServicesInfo(): ng.IPromise {
    let promise = this.$http({'method': 'OPTIONS', 'url': '/api/'});
    let infoPromise = promise.then((response: any) => {
      this.servicesInfo = response.data;
    });
    return infoPromise;
  }

  /**
   * Returns services information.
   *
   * @returns {any} servies information
   */
  getServicesInfo(): any {
    return this.servicesInfo;
  }
}
