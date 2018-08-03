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

interface ICheListOnScrollBottomAttributes extends ng.IAttributes {
  cheListOnScrollBottom: any;
}

/**
 * Defines a directive for a scrolled to bottom list event.
 * @author Michail Kuznyetsov
 */
export class CheListOnScrollBottom {
  restrict = 'A';

  /**
   * Check if scroll has reached the bottom
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheListOnScrollBottomAttributes) {
    const raw = $element[0];
    $element.bind('scroll', function () {
      if (raw.scrollTop + raw.offsetHeight - raw.scrollHeight >= 0) {
        $scope.$apply($attrs.cheListOnScrollBottom);
      }
    });
  }
}
