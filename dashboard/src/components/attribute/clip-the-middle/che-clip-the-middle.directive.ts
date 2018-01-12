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

interface ICheClipTheMiddleAttributes extends ng.IAttributes {
  str: string;
}

/**
 * @ngdoc directive
 * @name components.directive:cheClipTheMiddle
 * @restrict AE
 * @function
 * @element
 *
 * @usage
 *  <span che-clip-the-middle>Very long long long long long string</span>
 *
 *
 * @description
 * `che-clip-the-middle` cuts off the middle of very long string and puts ellipses instead
 *
 * @author Oleksii Kurinnyi
 */
export class CheClipTheMiddle implements ng.IDirective {
  restrict: string = 'AE';
  replace: boolean = true;

  template($element: ng.IAugmentedJQuery): string {
    const str = $element.text();
    return `
<div data-str="${str}" class="che-clip-the-middle">
  <span class="che-clip-the-middle-start"></span>
  <span class="che-clip-the-middle-end"></span>
</div>
    `;
  }

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheClipTheMiddleAttributes): void {
    const str = $attrs.str,
      strStart = str.substr(0, str.length - 3),
      strEnd = str.substr(str.length - 3, 3);

    $element.find('.che-clip-the-middle-start').text(strStart);
    $element.find('.che-clip-the-middle-end').text(strEnd);
  }

}
