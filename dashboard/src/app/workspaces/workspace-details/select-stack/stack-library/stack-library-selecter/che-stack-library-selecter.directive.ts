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

interface ISelecterScope extends ng.IScope {
  stackId: string;
  select: Function;
}

/**
 * Defines a directive for the stack library selecter.
 * @author Florent Benoit
 */
export class CheStackLibrarySelecter implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/select-stack/stack-library/stack-library-selecter/che-stack-library-selecter.html';

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor () {
    // scope values
    this.scope = {
      title: '@cheTitle',
      text: '@cheText',
      extraText: '@cheExtraText',
      stackId: '@cheStackId',
      isActive: '=cheIsActive',
      isSelect: '=cheIsSelect'
    };
  }

  link($scope: ISelecterScope, element: ng.IAugmentedJQuery) {
    // select item
    $scope.select = () => {
      $scope.$emit('event:library:selectStackId', $scope.stackId);
    };

    const jqTextWrapper  = element.find('.che-stack-library-selecter-descr'),
          jqText         = angular.element(jqTextWrapper.find('.che-stack-library-selecter-descr-text')),
          jqTextEllipsis = angular.element(jqTextWrapper.find('.che-stack-library-selecter-descr-ellipsis'));

    // show ellipsis
    $scope.$watch(() => {
      return jqText.get(0).clientHeight;
    }, (textHeight: number) => {
      if (textHeight > jqTextWrapper.get(0).clientHeight) {
        jqTextEllipsis.show();
      } else {
        jqTextEllipsis.hide();
      }
    });
  }

}
