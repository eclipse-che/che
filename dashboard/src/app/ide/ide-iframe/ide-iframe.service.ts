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

/*global $:false */

/**
 * Defines a service for displaying iframe for displaying the IDE.
 * @author Florent Benoit
 */
class IdeIFrameSvc {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout, $compile, $rootScope, $location, $window, cheUIElementsInjectorService) {
    this.cheUIElementsInjectorService = cheUIElementsInjectorService;
    this.$timeout = $timeout;
    this.$compile = $compile;
    this.$location = $location;

    $window.addEventListener("message", (event) => {
      if ("show-ide" === event.data) {
        // check whether user is still waiting for IDE
        if (/\/ide\//.test($location.path())) {
          $rootScope.$apply(() => {
            $rootScope.showIDE = true;
            $rootScope.hideLoader = true;
          });
        }
      } else if ("show-workspaces" === event.data){
        $rootScope.$apply(() => {
          $location.path('/workspaces');
        });
      }
    }, false);
  }

}

export default IdeIFrameSvc;
