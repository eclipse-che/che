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

export interface IPlugin {
  id: string;
  name: string;
  publisher: string;
  deprecate?: {
    automigrate: boolean;
    migrateTo: string;
  };
  displayName: string;
  type: string;
  version?: string;
  description?: string;
  isEnabled?: boolean;
}

export interface IPluginRow {
  id: string;
  name: string;
  displayName: string;
  description: string;
  publisher: string;
  isEnabled?: boolean;
  isDeprecated?: boolean;
  selected: string;
  versions: {
    value: string;
    label: string;
  }[];
}

/**
 * This class is handling plugin registry api
 * @author Ann Shumilova
 */
export class PluginRegistry {
  static $inject = ['$http', '$q'];

  /**
   * Angular Http service.
   */
  private $http: ng.IHttpService;
  /**
   * Angular promise service.
   */
  private $q: ng.IQService;

  private plugins = new Map<string, Array<IPlugin>>();

  private headers: { [name: string]: string; };

  /**
   * Default constructor that is using resource
   */
  constructor($http: ng.IHttpService, $q: ng.IQService) {
    this.$http = $http;
    this.$q = $q;

    this.headers = { 'Authorization': undefined };
  }

  static get EDITOR_TYPE(): string {
    return 'Che Editor';
  }

  fetchPlugins(location: string): ng.IPromise<Array<IPlugin>> {
    const promise = this.$http({
      'method': 'GET',
      'url': `${location}/plugins/`,
      'headers': this.headers
    });

    return promise.then((result: ng.IHttpResponse<IPlugin[]>) => {
      this.plugins.set(location, result.data);
      return this.$q.when(result.data);
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.$q.when(this.plugins.get(location));
      }
      return this.$q.reject(error);
    });
  }

  getPlugins(location: string): Array<IPlugin> {
    return this.plugins.get(location);
  }

  hasPlugins(location: string): boolean {
    return this.plugins.has(location);
  }
}
