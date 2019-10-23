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
 * This class is handling the che devfile JSON Schema
 * @author Josh Pinkney
 */
export class CheDevfile {

  static $inject = ['$http'];

  private $http: ng.IHttpService;
  private devfileJSONSchemaPromise: ng.IPromise<any>;

  /**
   * Default constructor that is using resource
   */
  constructor ($http: ng.IHttpService) {
    this.$http = $http;
  }

  /**
   * Retrieve the current JSON Schema of the devfile
   */
  fetchDevfileSchema(): ng.IPromise<any> {
    if (this.devfileJSONSchemaPromise) {
      return this.devfileJSONSchemaPromise;
    }

    let promise = this.$http.get('/api/devfile/');
    this.devfileJSONSchemaPromise = promise.then((devfileJSONSchema: any) => {
      return devfileJSONSchema.data;
    });
    return this.devfileJSONSchemaPromise;
  }

}
