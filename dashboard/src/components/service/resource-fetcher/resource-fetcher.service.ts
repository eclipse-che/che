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

import { CheBranding } from '../../branding/che-branding';

/**
 * Provides a way to download IDE .js and then cache it before trying to load the IDE
 * @author Florent Benoit
 * @author Oleksii Orel
 * @author David Festal
 */
export class ResourceFetcherService {

  static $inject = [
    '$log',
    '$http',
    'cheBranding'
  ];

  private $log: ng.ILogService;
  private $http: ng.IHttpService;
  private cheBranding: CheBranding;

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    $log: ng.ILogService,
    $http: ng.IHttpService,
    cheBranding: CheBranding
  ) {
    this.$log = $log;
    this.$http = $http;
    this.cheBranding = cheBranding;

    const prefetch = this.cheBranding.getConfiguration().prefetch;
    this.prefetchCheCDNResources(prefetch.cheCDN);
    this.prefetchResources(prefetch.resources);
  }

  /**
   * Prefetch Che specific resources.
   */
  private prefetchCheCDNResources(url: string): void {
    if (!url) {
      return;
    }
    type resourceEntry = {
      chunk: string;
      cdn: string;
    };
    this.$http.get(url, { cache: false }).then((response: ng.IHttpResponse<Array<resourceEntry>>) => {
      if (!response || !response.data) {
        return;
      }
      response.data.forEach(entry => {
        // load the url
        if (angular.isDefined(entry.cdn)) {
          this.appendLink(entry.cdn);
        } else {
          this.$log.error('Unable to find the Theia resource file to cache');
        }
      });
    }, (error: any) => {
      this.$log.log(`Unable to find Theia CDN resources to cache`, error);
    });
  }

  /**
   * Prefetch other resources
   */
  private prefetchResources(resources: string[]): void {
    if (!resources || resources.length === 0) {
      return;
    }
    resources.forEach(resource => {
      this.appendLink(resource);
    });
  }

  /**
   * Appends the `link` node linked to a resource
   */
  private appendLink(url: string): void {
    this.$log.log('Preloading resource', url);
    const link = document.createElement('link');
    link.rel = 'prefetch';
    link.href = url;
    document.head.appendChild(link);
  }

}
