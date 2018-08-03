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
import {CheBranding} from '../branding/che-branding.factory';

const IDE_FETCHER_CALLBACK_ID = 'cheIdeFetcherCallback';

/**
 * Provides a way to download IDE .js and then cache it before trying to load the IDE
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CheIdeFetcher {

  static $inject = ['$log', '$http', '$window', 'cheBranding'];

  private $log: ng.ILogService;
  private $http: ng.IHttpService;
  private $window: ng.IWindowService;
  private cheBranding: CheBranding;
  private userAgent: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($log: ng.ILogService, $http: ng.IHttpService, $window: ng.IWindowService, cheBranding: CheBranding) {
    this.$log = $log;
    this.$http = $http;
    this.$window = $window;
    this.cheBranding = cheBranding;

    this.userAgent = this.getUserAgent();

    const callback = () => {
      this.findMappingFile();
      this.cheBranding.unregisterCallback(IDE_FETCHER_CALLBACK_ID);
    };
    this.cheBranding.registerCallback(IDE_FETCHER_CALLBACK_ID, callback.bind(this));
  }

  /**
   * Gets user agent.
   * @returns {string}
   */
  getUserAgent(): string {
    const userAgent = this.$window.navigator.userAgent.toLowerCase();
    const docMode = (<any>this.$window.document).documentMode;

    if (userAgent.indexOf('webkit') !== -1) {
      return 'safari';
    } else if (userAgent.indexOf('msie') !== -1) {
      if (docMode >= 10 && docMode < 11) {
        return 'ie10';
      } else if (docMode >= 9 && docMode < 11) {
        return 'ie9';
      } else if (docMode >= 8 && docMode < 11) {
        return 'ie8';
      }
    } else if (userAgent.indexOf('gecko') !== -1) {
      return 'gecko1_8';
    }

    return 'unknown';
  }

  /**
   * Finds mapping file.
   */
  findMappingFile(): void {
    // get the content of the compilation mapping file
    const randVal = Math.floor((Math.random() * 1000000) + 1);
    const resourcesPath = this.cheBranding.getIdeResourcesPath();
    if (!resourcesPath) {
      this.$log.log('Unable to get IDE resources path');
      return;
    }

    const fileMappingUrl = `${resourcesPath}compilation-mappings.txt?uid=${randVal}`;

    this.$http.get(fileMappingUrl).then((response: {data: any}) => {
      if (!response || !response.data) {
        return;
      }
      let urlToLoad = this.getIdeUrlMappingFile(response.data);
      // load the url
      if (angular.isDefined(urlToLoad)) {
        this.$log.log('Preloading IDE javascript', urlToLoad);
        this.$http.get(urlToLoad, { cache: true});
      } else {
        this.$log.error('Unable to find the IDE javascript file to cache');
      }
    }, (error: any) => {
      this.$log.log('unable to find compilation mapping file', error);
    });
  }

  /**
   * Gets URL of mapping file.
   * @param data {string}
   * @returns {string}
   */
  getIdeUrlMappingFile(data: string): string {
    let mappingFileUrl: string;
    let javascriptFileName: string;
    const mappings = data.split(new RegExp('^\\n', 'gm'));
    const isPasses = mappings.some((mapping: string) => {
      const subMappings = mapping.split('\n');
      const userAgent = subMappings.find((subMapping: string) => {
        return subMapping.startsWith('user.agent ');
      }).split(' ')[1];
      javascriptFileName = subMappings.find((subMapping: string) => {
        return subMapping.endsWith('.cache.js');
      });
      return javascriptFileName && userAgent && this.userAgent === userAgent;
    });
    if (isPasses && javascriptFileName) {
      mappingFileUrl = this.cheBranding.getIdeResourcesPath() + javascriptFileName;
    }

    return  mappingFileUrl;
  }

}
