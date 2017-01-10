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

/**
 * Defines a directive for Accordion component
 * @author Oleksii Kurinnyi
 */
export class CheAccordion {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout) {
    this.$timeout = $timeout;
    this.restrict = 'E';
    this.transclude = true;
    this.replace = true;
    this.template = '<div ng-transclude class="che-accordion che-accordion-closed"></div>';

    // scope values
    this.scope = {
      openCondition: '=cheOpenCondition'
    };
  }

  link($scope, element, attr, ctrl) {
    let currentBodyElement = element.find('.che-accordion-body');

    // automatic switching panes
    $scope.$watch(() => {
      return $scope.openCondition;
    }, (doOpenPane) => {
      if (!element.siblings().hasClass('che-accordion-dirty')) {
        openPane(doOpenPane);
      }
    });

    // manual switching panes
    element.bind('click', (event) => {
      if (angular.element(event.target).parent().hasClass('che-accordion-title')) {
        element.addClass('che-accordion-dirty');
        openPane(true);
      }
    });

    let openPane = (doOpenPane) => {
      if (element.hasClass('che-accordion-closed')) {
        let siblingElements = element.siblings(),
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
          panesToClose.forEach((pane) => {
            pane.addClass('che-accordion-closed');
          });

          if (doOpenPane) {
            let currentBodyHeight = currentBodyElement[0].scrollHeight;
            if (currentBodyElement[0].clientHeight !== currentBodyHeight) {
              currentBodyElement.css('height', currentBodyHeight);
            }

            // open current pane
            element.removeClass('che-accordion-closed');
          }
        },10).then(() => {
          for (let i=0; i<siblingElements.length; i++) {
            angular.element(siblingElements[i]).find('.che-accordion-body').removeAttr('style');
          }
        });
      }
    }
  }
}
