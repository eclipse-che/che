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
import {CheLearnMoreCtrl} from './che-learn-more.controller';

interface ICheLearnMoreItemAttributes extends ng.IAttributes {
  cheLearnMoreKey: any;
}

/**
 * @ngdoc directive
 * @name components.directive:cheLearnMoreItem
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-learn-more>` defines a learn more item.
 *
 * @author Florent Benoit
 */
export class CheLearnMoreItem implements ng.IDirective {

  restrict = 'E';

  require = '^cheLearnMore';
  terminal = true;

  scope = { };

  /**
   * Defines id of the controller and apply some initial settings
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attributes: ICheLearnMoreItemAttributes, controller: CheLearnMoreCtrl): void {
    const items = $element.parent()[0].getElementsByTagName('che-learn-more-item');
    const index = Array.prototype.indexOf.call(items, $element[ 0 ]);

    const title = $element.find('che-learn-more-item-title').eq(0).remove();
    const content  = $element.find('che-learn-more-item-content').eq(0).remove();
    const key = $attributes.cheLearnMoreKey;

    controller.insertItem({
        scope:    $scope,
        parent:   $scope.$parent,
        index:    index,
        element:  $element,
        key: key,
        content: content.html(),
        title:    title.html()
    });
  }

}
