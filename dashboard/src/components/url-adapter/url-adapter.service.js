/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mario Loriedo - initial implementation
 */
'use strict';

/**
 * Adapt an Url to the particular infrastructure:
 *  - Replace the Url hostname with the value in browser address bar
 *  - Use the hostname provided by the Che API
 * @author Mario Loriedo
 */
export class UrlAdapter {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location) {
    this.$location = $location;
  }

  /**
   * Replace the hostname in url with the hostname from the browser address bar
   * @param url {String} URL to fix
   * @returns {String}
   */
  fixHostName(oldurl) {
    if ( !oldurl ) {
      return '';
    } else if (this.$location) {
      let a = document.createElement('a');
      a.href = oldurl;
      a.hostname = this.$location.host();
      return a.href;
    } else {
      return oldurl;
    }
  }
}
