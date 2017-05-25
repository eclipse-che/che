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
import {CheStackLibraryFilterController} from './che-stack-library-filter.controller';

/**
 * @ngdoc directive
 * @name workspace.create.directive:cheStackLibraryFilter
 * @restrict E
 * @element
 *
 * @description
 * `<che-stack-library-filter></che-stack-library-filter>` for creating new tags filter
 *
 * @author Oleksii Kurinnyi
 */
export class CheStackLibraryFilter implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/create-workspace/stack-selector/stack-library-filter/che-stack-library-filter.html';
  controller = 'CheStackLibraryFilterController';
  controllerAs = 'cheStackLibraryFilterCtrl';
  bindToController = true;
  scope = {
    stackTags: '=',
    selectedTags: '=?',
    onTagsChanges: '&'
  };


  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, attrs: ng.IAttributes, ctrl: CheStackLibraryFilterController) {
    ctrl.selectSuggestion = (index: number) => {
      let selectionClass = 'stack-library-filter-suggestion-selected',
        suggestionElements = $element.find('.stack-library-filter-suggestions md-chip');

      // clear previously selected suggestion
      suggestionElements.removeClass(selectionClass);

      // set selection to specified suggestion
      angular.element(suggestionElements[index]).addClass(selectionClass);
    };

    // select suggestion by keys
    $element.bind('keypress keydown', (event: any) => {
      if (event.which === 38) {
        // on press 'up'
        // select prev suggestion
        event.preventDefault();

        ctrl.selectedIndex--;
        if (ctrl.selectedIndex < 0) {
          ctrl.selectedIndex = ctrl.suggestions.length - 1;
        }
        ctrl.selectSuggestion(ctrl.selectedIndex);
      } else if (event.which === 9 || event.which === 40) {
        // on press 'tab' or 'down'
        // select next suggestion
        event.preventDefault();

        ctrl.selectedIndex++;
        if (ctrl.selectedIndex > ctrl.suggestions.length - 1) {
          ctrl.selectedIndex = 0;
        }
        ctrl.selectSuggestion(ctrl.selectedIndex);
      }
    });
  }
}
