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

import {HttpInterceptorBase} from './interceptor-base';

/**
 * @author Oleksii Kurinnyi
 */
export class ETagInterceptor extends HttpInterceptorBase {
  static $inject = ['$q'];

  $q: ng.IQService;

  etagMap: any;

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService) {
    super();

    this.$q = $q;

    this.etagMap = {};
  }

  request(config: any): any {
    // add IfNoneMatch request on the che api if there is an existing eTag
    if ('GET' === config.method) {
      if (config.url.indexOf('/api') === 0) {
        const eTagURI = this.etagMap[config.url];
        if (eTagURI) {
          config.headers = config.headers || {};
          angular.extend(config.headers, {'If-None-Match': eTagURI});
        }
      }
    }
    return config || this.$q.when(config);
  }

  response(response: any): any {
    // if response is ok, keep ETag
    if ('GET' === response.config.method) {
      if (response.status === 200) {
        const responseEtag = response.headers().etag;
        if (responseEtag) {
          if (response.config.url.indexOf('/api') === 0) {
            this.etagMap[response.config.url] = responseEtag;
          }
        }
      }

    }
    return response || this.$q.when(response);
  }

}
