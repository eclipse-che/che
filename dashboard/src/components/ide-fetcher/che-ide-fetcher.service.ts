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
 * Provides a way to download IDE .js and then cache it before trying to load the IDE
 * @author Florent Benoit
 */
export class CheIdeFetcher {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($http, $window, cheBranding) {
    this.$http = $http;
    this.$window = $window;
    this.cheBranding = cheBranding;

    this.userAgent = this.getUserAgent();
    this.cheBranding.promise.then(() => {
      this.findMappingFile();
    }, (error) => {
      console.log('error with promise,', error);
    });
  }


  getUserAgent() {
    var userAgent = this.$window.navigator.userAgent.toLowerCase();
    var docMode = this.$window.document.documentMode;

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


  findMappingFile() {
    // get the content of the compilation mapping file
    let randVal = Math.floor((Math.random()*1000000)+1);
    let rand = '?uid=' + randVal;
    let resourcesPath = this.cheBranding.getIdeResourcesPath();
    if (!resourcesPath) {
      console.log('Unable to get IDE resources path');
      return;
    }

    let fileMappinUrl = resourcesPath + 'compilation-mappings.txt' + rand;

    let promise = this.$http.get(fileMappinUrl);

    this.dataPromise = promise.then((response) => {

      let urlToLoad = this.getIdeUrlMappingFile(response.data);

      // load the url
      if (urlToLoad != null) {
        console.log('Preloading IDE javascript', urlToLoad);
        this.$http.get(urlToLoad, { cache: true});
      } else {
        console.error('Unable to find the IDE javascript file to cache');
      }
    }, (error) => {
      console.log('unable to find compilation mapping file', error);
    });

  }

  getIdeUrlMappingFile(data) {

    var mappings = data.split('\n');
    var lineIndex = 0;

    var javascriptFilename = null;
    var userAgent = null;

    while (lineIndex < mappings.length) {

      var currentLine = mappings[lineIndex];

      if (currentLine === '') {
        if (javascriptFilename && userAgent && this.userAgent === userAgent.split(' ')[1]) {
          /// ide-resources/_app/_app.nocache.js
          return this.cheBranding.getIdeResourcesPath() + javascriptFilename;
        }

        // reset current variables
        javascriptFilename = null;
        userAgent = null;
      }

      if (currentLine.endsWith('.cache.js')) {
        javascriptFilename = mappings[lineIndex];
      }

      if (currentLine.startsWith('user.agent ')) {
        userAgent = mappings[lineIndex];
      }

      lineIndex++;
    }

    return null;

  }

}
