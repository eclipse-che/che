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
 * @ngdoc factory
 * @name components.routing.factory:routingRedirect
 * @author Florent Benoit
 */
export class RoutingRedirect {

  static $inject = ['$location'];

  $location: ng.ILocationService;
  routeCallbacks: Array<any>;

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService) {
    this.$location = $location;
    this.routeCallbacks = [];
  }

  /**
   * Add a givan callback to this routing disabler
   * @param {any} routeCallback
   */
  addRouteCallback(routeCallback: any): void {
    this.routeCallbacks.push(routeCallback);
  }

  /**
   * Check the given event with the given next object
   * @param event the routing event that can be cancelled
   * @param next the expected route
   */
  check(event: any, next: any) {

    // loop routes and check if pages are authorized
    let i = 0;
    while (i < this.routeCallbacks.length) {
      let routeCallback = this.routeCallbacks[i];

      // if page is authorized, nothing to check
      if (routeCallback.checkPage(next.templateUrl)) {
        return;
      }
      i++;
    }

    // ok now page may not be accessible and need to be redirected
    i = 0;
    let foundSkip = false;
    while (i < this.routeCallbacks.length && !foundSkip) {

      let routeCallback = this.routeCallbacks[i];

      let answer = routeCallback.checkRedirect(next.templateUrl);

      // continue routing as there is no routing blocker
      // there was an answer, route to this path
      if (answer.route) {
        this.$location.path(answer.route);
        event.preventDefault();
        foundSkip = true;
      }
      i++;
    }

  }

}
