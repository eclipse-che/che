/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
