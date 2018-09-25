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

export interface ISearchResults {
  total: number;
  results: Array<ISearchResult>;
}

export interface ISearchResult {
  package: IPackage;
  flags: Object;
  score: Object;
  searchScore: Object;
}

export interface IPackage {
  name: string;
  scope?: string;
  version?: string;
  description?: string;
  keywords?: Array<string>;
  date?: string;
  links?: Object;
  author?: Object;
  publisher?: Object;
  maintainers?: Array<Object>;
  isEnabled?: boolean;
}


/**
 * This class is handling npm registry api
 * @author Ann Shumilova
 */
export class NpmRegistry {
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

  search(keyword: string): ng.IPromise<ISearchResults> {
    let promise = this.$http({'method': 'GET', 'url': 'https://api.npms.io/v2/search?q=' + keyword + '&size=' + 50});
    return promise.then((result: any) => {
      return result.data;
    });
  }
}
