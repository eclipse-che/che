/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

interface ICheReloadHrefAttributes extends ng.IAttributes {
  href: string;
}

/**
 * @ngdoc directive
 * @name components.directive:cheReloadHref
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-reload-href` defines an attribute for auto reloading the link if it is the same than current route.
 *
 * @usage
 *   <a che-reload-href="myLink"></a>
 *
 * @author Florent Benoit
 */
export class CheReloadHref implements ng.IDirective {

  static $inject = ['$location', '$route'];

  restrict = 'A';

  $location: ng.ILocationService;
  $route: ng.route.IRouteService;

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService,
              $route: ng.route.IRouteService) {
    this.restrict = 'A';
    this.$location = $location;
    this.$route = $route;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheReloadHrefAttributes) {
    if ($attrs.href) {
      $element.bind('click', () => {
        $scope.$apply(() => {
          if (this.$location.path() === $attrs.href || ('#' + this.$location.path()) === $attrs.href) {
            console.log('reloading the route...');
            this.$route.reload();
          }
        });
      });
    }
  }

}
