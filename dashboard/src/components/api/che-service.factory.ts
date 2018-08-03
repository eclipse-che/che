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
 * This class is handling the all services API retrieval.
 * @author Ann Shumilova
 */
export class CheService {

  static $inject = ['$http'];

  /**
   * Service for performing HTTP requests.
   */
  private $http: ng.IHttpService;

  private servicesPromise: ng.IPromise<any>;
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
   */
  constructor ($http: ng.IHttpService) {
    this.$http = $http;

    this.fetchServices();
  }

  /**
   * Fetches all available services.
   *
   * @returns {ng.IPromise<any>}
   */
  fetchServices(): ng.IPromise<any> {
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
   * @returns {IHttpPromise<any>}
   */
  fetchServicesInfo(): ng.IPromise<any> {
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
