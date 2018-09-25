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

interface ICheFocusableAttributes extends ng.IAttributes {
  focusable: boolean;
}

/**
 * Defines a directive for creating focusable attribute.
 * @author Oleksii Orel
 */
export class CheFocusable implements ng.IDirective {

  restrict = 'A';

  /**
   * Keep reference to the model controller
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheFocusableAttributes): void {
    $scope.$watch(() => { return $attrs.focusable; }, (newVal: boolean) => {
      if (!newVal) {
        return;
      }
      $element.eq(0).focus();
    });
  }
}
