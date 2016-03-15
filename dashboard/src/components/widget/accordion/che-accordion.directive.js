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
    this.template = '<div ng-transclude class="che-accordion closed"></div>';

    // scope values
    this.scope = {
      index: '@cheIndex',
      step: '@cheCurrentStep'
    };
  }

  link($scope, element) {
    let currentBodyElement = element.find('.che-accordion-body');

    // automatic switching panes
    $scope.$watch(() => {
      return $scope.step;
    }, (newVal) => {
      if ($scope.index === newVal) {
        element.siblings().removeClass('active');
        element.addClass('active');
      }

      if (!element.siblings().hasClass('dirty') && $scope.index === newVal) {
        openPane();
      }
    });

    // manual switching panes
    element.bind('click', (event) => {
      if (angular.element(event.target).parent().hasClass('che-accordion-title')) {
        element.addClass('dirty');
        openPane(element);
      }
    });

    currentBodyElement.bind('transitionend', () => {
      currentBodyElement.removeAttr('style');
    });

    let openPane = () => {
      if (element.hasClass('closed')) {
        let siblingElements = element.siblings(),
          panesToClose = [];

        // find opened pane and close it
        for (let i = 0; i < siblingElements.length; i++) {
          let siblingEl = angular.element(siblingElements[i]);
          if (siblingEl.hasClass('closed')) {
            continue;
          }

          let siblingBodyEl = siblingEl.find('.che-accordion-body'),
            siblingBodyHeight = siblingBodyEl[0].scrollHeight;
          siblingBodyEl.css('height', siblingBodyHeight);
          panesToClose.push(siblingEl);
        }

        // open current pane
        let currentBodyHeight = currentBodyElement[0].scrollHeight;
        currentBodyElement.css('height', currentBodyHeight);

        this.$timeout(() => {
          panesToClose.forEach((pane) => {
            pane.addClass('closed');
          });
          element.removeClass('closed');
        },0);
      }
    }
  }
}
