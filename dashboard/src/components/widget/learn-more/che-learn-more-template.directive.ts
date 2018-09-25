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

interface ICheLearmMoreTemplateScope extends ng.IScope {
  compileScope: any;
  template: string;
}

/**
 * @ngdoc directive
 * @name components.directive:cheLearnMoreTemplate
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-learn-more>` defines a learn more item.
 *
 * @author Florent Benoit
 */
export class CheLearnMoreTemplate implements ng.IDirective {

  static $inject = ['$compile', '$mdUtil'];

  $compile: ng.ICompileService;
  $mdUtil: any;

  restrict = 'A';

  require = '^cheLearnMore';

  scope = {
    template: '=cheLearnMoreTemplate',
    compileScope: '=cheScope'
  };

  /**
   * Default constructor that is using resource
   */
  constructor ($compile: ng.ICompileService, $mdUtil: any) {
    this.$compile = $compile;
    this.$mdUtil = $mdUtil;
  }

  /**
   * Defines id of the controller and apply some initial settings
   */
  link($scope: ICheLearmMoreTemplateScope, $element: ng.IAugmentedJQuery) {
    const compileScope = $scope.compileScope;
    $element.html($scope.template);
    this.$compile($element.contents())(compileScope);
  }

}
