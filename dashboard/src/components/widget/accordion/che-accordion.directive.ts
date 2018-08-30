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

interface ICheAccordionScope extends ng.IScope {
  openCondition: boolean;
}

/**
 * Defines a directive for Accordion component
 * @author Oleksii Kurinnyi
 */
export class CheAccordion implements ng.IDirective {

  static $inject = ['$timeout'];

  restrict = 'E';
  transclude = true;
  replace = true;
  template = '<div ng-transclude class="che-accordion che-accordion-closed"></div>';

  // scope values
  scope = {
    openCondition: '=cheOpenCondition'
  };

  $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  link($scope: ICheAccordionScope, $element: ng.IAugmentedJQuery): void {
    let currentBodyElement = $element.find('.che-accordion-body');

    let openPane = (doOpenPane: boolean) => {
      if ($element.hasClass('che-accordion-closed')) {
        let siblingElements = $element.siblings(),
          panesToClose = [];

        // find opened pane and close it
        for (let i = 0; i < siblingElements.length; i++) {
          let siblingEl = angular.element(siblingElements[i]);
          if (siblingEl.hasClass('che-accordion-closed')) {
            continue;
          }

          let siblingBodyEl = siblingEl.find('.che-accordion-body'),
            siblingBodyHeight = siblingBodyEl[0].clientHeight;
          siblingBodyEl.css('height', siblingBodyHeight);
          panesToClose.push(siblingEl);
        }

        this.$timeout(() => {
          // close other panes
          panesToClose.forEach((pane: ng.IAugmentedJQuery) => {
            pane.addClass('che-accordion-closed');
          });

          if (doOpenPane) {
            let currentBodyHeight = currentBodyElement[0].scrollHeight;
            if (currentBodyElement[0].clientHeight !== currentBodyHeight) {
              currentBodyElement.css('height', currentBodyHeight);
            }

            // open current pane
            $element.removeClass('che-accordion-closed');
          }
        }, 10).then(() => {
          for (let i = 0; i < siblingElements.length; i++) {
            angular.element(siblingElements[i]).find('.che-accordion-body').removeAttr('style');
          }
        });
      }
    };

    // automatic switching panes
    $scope.$watch(() => {
      return $scope.openCondition;
    }, (doOpenPane: boolean) => {
      if (!$element.siblings().hasClass('che-accordion-dirty')) {
        openPane(doOpenPane);
      }
    });

    // manual switching panes
    $element.bind('click', (event: JQueryEventObject) => {
      if (angular.element(event.target).parent().hasClass('che-accordion-title')) {
        $element.addClass('che-accordion-dirty');
        openPane(true);
      }
    });
  }
}
