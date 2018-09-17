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
  type: string;
  version: string;
  description?: string;
  isEnabled: boolean;
}


/**
 * This class is handling plugin registry api
 * @author Ann Shumilova
 */
export class PluginRegistry {
  static $inject = ['$http'];

  /**
   * Angular Http service.
   */
  private $http: ng.IHttpService;

  /**
   * Default constructor that is using resource
   */
  constructor($http: ng.IHttpService) {
    this.$http = $http;
  }

  fetchPlugins(location: string): ng.IPromise<Array<IPlugin>> {
    let promise = this.$http({'method': 'GET', 'url': location + '/plugins/'});
    return promise.then((result: any) => {
      return result.data;
    });
  }
}
