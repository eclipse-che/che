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
 * Fetches images using the $http service.
 *
 * @author Oleksii Kurinnyi
 */
export class ImgSrc implements ng.IDirective {

  static $inject = ['$http', 'userDashboardConfig'];

  $http: ng.IHttpService;
  isDev: boolean;

  restrict = 'A';

  /**
   * Default constructor that is using resource injection
   */
  constructor($http: ng.IHttpService, userDashboardConfig: any) {
    this.$http = $http;
    this.isDev = userDashboardConfig.developmentMode;
  }

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes): void {
    $attrs.$observe('imgSrc', (url: string) => {
      if (this.isDev) {
        url = url.replace(/https?:\/\/[^\/]+/, '');
      }

      const requestConfig = {
        method: 'GET',
        url: url,
        cache: 'true'
      };
      this.$http(requestConfig).then((response: any) => {
        const blob = new Blob([response.data], {type: response.headers('Content-Type')});
        $attrs.$set('src', (window.URL || (window as any).webkitURL).createObjectURL(blob));
      });
    });
  }

}
