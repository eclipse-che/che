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

export interface ICheSelecterScope extends ng.IScope {
  valueModel: string;
  title: string;
  options: any;
  values: any;
  name: string;
  icon: string;
  callbackController: any;
  $parent: any;
}

/**
 * Defines a directive for the selecter.
 * @author Florent Benoit
 */
export class CheSelecter implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'components/widget/selecter/che-selecter.html';

  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  controller = 'CheSelecterCtrl';
  controllerAs = 'cheSelecterCtrl';

  // scope values
  scope = {
    valueModel : '=ngModel',
    title: '@cheTitle',
    options: '@cheOptions', /* uses ngOptions syntax */
    values: '=cheValues', /* source of the select values _in parent scope_ */
    name: '@cheName',
    icon: '@cheIcon',
    callbackController: '=cheCallbackController' /* object with a cheSelecter(name, valueSelected) function, called when the selecter is selector or the select value changes */
  };

  link($scope: ICheSelecterScope, $element: ng.IAugmentedJQuery): void {
    // defines the first element as selected
    if ($scope.$parent.$first) {
        $scope.$parent.$parent[$scope.name + '.selecterSelected'] = $scope.title;
    }

    let selectElement = $element.find('select');
    // fixes: first click on select element is not handled as clicked event on whole selected component:
    selectElement.bind('mousedown', function() {
      selectElement.click();
    });
  }

}
