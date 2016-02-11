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
 * Defines a directive for the simple selecter.
 * @author Florent Benoit
 */
export class CheSimpleSelecter {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='E';
    //this.replace= true;
    //this.transclude= true;
    this.templateUrl = 'components/widget/simple-selecter/che-simple-selecter.html';


    this.controller = 'CheSimpleSelecterCtrl';
    this.controllerAs = 'cheSimpleSelecterCtrl';
    this.bindToController = true;


    // scope values
    this.scope = {
      title: '@cheTitle',
      description: '@cheDescription',
      isFirst : '=cheIsFirst',
      value: '=cheValue',
      name: '@cheName',
      icon: '@cheIcon',
      callbackController: '=cheCallbackController' /* object with a cheSimpleSelecter(name) function, called when the selecter is selector or the select value changes */
    };


  }


  link($scope, element, attrs) {
    // defines property name
    $scope.selectName = attrs.cheName;


    // defines the first element as selected
    if ($scope.$parent.$first) {
      $scope.$parent.$parent[attrs.cheName + '.selecterSelected'] = attrs.cheTitle;
    }


  }


}
