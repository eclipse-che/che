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
import {CheUIElementsInjectorService} from '../../../components/service/injector/che-ui-elements-injector.service';

/*global $:false */

interface IIdeIFrameRootScope extends ng.IRootScopeService {
  showIDE: boolean;
  hideLoader: boolean;
  hideNavbar: boolean;
}


/**
 * Defines a service for displaying iframe for displaying the IDE.
 * @author Florent Benoit
 */
class IdeIFrameSvc {
  static $inject = ['$window', '$timeout', '$compile', '$location', '$rootScope', '$mdSidenav', 'cheUIElementsInjectorService'];

  private cheUIElementsInjectorService: CheUIElementsInjectorService;
  private $timeout: ng.ITimeoutService;
  private $compile: ng.ICompileService;
  private $location: ng.ILocationService;
  private $mdSidenav: ng.material.ISidenavService;

  /**
   * Default constructor that is using resource
   */
  constructor($window: ng.IWindowService,
              $timeout: ng.ITimeoutService,
              $compile: ng.ICompileService,
              $location: ng.ILocationService,
              $rootScope: IIdeIFrameRootScope,
              $mdSidenav: ng.material.ISidenavService,
              cheUIElementsInjectorService: CheUIElementsInjectorService) {
    this.$timeout = $timeout;
    this.$compile = $compile;
    this.$location = $location;
    this.$mdSidenav = $mdSidenav;
    this.cheUIElementsInjectorService = cheUIElementsInjectorService;

    $window.addEventListener('message', (event: any) => {
      if ('show-ide' === event.data) {
        // check whether user is still waiting for IDE
        if (/\/ide\//.test($location.path())) {
          $rootScope.$apply(() => {
            $rootScope.showIDE = true;
            $rootScope.hideLoader = true;
          });
        }

      } else if ('show-workspaces' === event.data) {
        $rootScope.$apply(() => {
          $location.path('/workspaces');
        });

      } else if ('show-navbar' === event.data) {
        $rootScope.hideNavbar = false;
        $mdSidenav('left').open();

      } else if ('hide-navbar' === event.data) {
        $rootScope.hideNavbar = true;
        $mdSidenav('left').close();
      }

    }, false);
  }

}

export default IdeIFrameSvc;
