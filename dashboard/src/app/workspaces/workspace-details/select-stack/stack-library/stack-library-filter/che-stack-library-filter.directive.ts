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

/**
 * @ngdoc directive
 * @name projects.create.directive:cheStackLibraryFilterCtrl
 * @restrict E
 * @element
 *
 * @description
 * `<che-stack-library-filter></che-stack-library-filter>` for creating new tags filter
 *
 * @author Oleksii Kurinnyi
 */
export class CheStackLibraryFilter {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/workspace-details/select-stack/stack-library/stack-library-filter/che-stack-library-filter.html';

    this.controller = 'CheStackLibraryFilterController';
    this.controllerAs = 'cheStackLibraryFilterCtrl';

    // we require ngModel as we want to use it inside our directive
    this.require = 'ngModel';

    this.bindToController = true;

    this.scope = {
      ngModel: '='
    };
  }


  link($scope, element) {
    let ctrl = $scope.cheStackLibraryFilterCtrl;
    let selectSuggestion = (element, index) => {
      let selectionClass = 'stack-library-filter-suggestion-selected',
        suggestionElements = element.find('.stack-library-filter-suggestions md-chip');

      // clear previously selected suggestion
      suggestionElements.removeClass(selectionClass);

      // set selection to specified suggestion
      angular.element(suggestionElements[index]).addClass(selectionClass);
    };

    // select suggestion by keys
    element.bind('keypress keydown', (event) => {
      if (event.which === 38) {
        // on press 'up'
        // select prev suggestion
        event.preventDefault();

        ctrl.selectedIndex--;
        if (ctrl.selectedIndex < 0) {
          ctrl.selectedIndex = ctrl.suggestions.length-1;
        }
        selectSuggestion(element, ctrl.selectedIndex);
      }
      else if (event.which === 9 || event.which === 40) {
        // on press 'tab' or 'down'
        // select next suggestion
        event.preventDefault();

        ctrl.selectedIndex++;
        if (ctrl.selectedIndex > ctrl.suggestions.length-1) {
          ctrl.selectedIndex = 0;
        }
        selectSuggestion(element, ctrl.selectedIndex);
      }
    });

    // set first suggestion selected
    $scope.$on('selectSuggestion', () => {
      selectSuggestion(element, 0);
    });
  }
}
