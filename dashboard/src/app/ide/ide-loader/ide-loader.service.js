/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/*global $:false

 */
/**
 * Defines a service for displaying loader before displaying the IDE.
 * @author Florent Benoit
 */
class IdeLoaderSvc {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout, $compile) {
    this.isLoaderAdded = false;
    this.$timeout = $timeout;
    this.$compile = $compile;

  }


  addLoader() {
    if (!this.isLoaderAdded) {
      this.isLoaderAdded = true;
      // The new element to be added
      var $div = $('<ide-loader id="ide-loader" class="ide-loader-window" ng-hide="hideIdeLoader"></ide-loader>');

      // The parent of the new element
      var $target = $('body');

      let $scope = angular.element($target).scope();
      let insertHtml = this.$compile($div)($scope);
      $target.append(insertHtml);
    }
  }


}

export default IdeLoaderSvc;

