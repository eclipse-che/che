/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * Defines a directive for the stack library selecter.
 * @author Florent Benoit
 */
export class CheStackLibrarySelecter {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='E';
    this.templateUrl = 'app/workspaces/workspace-details/select-stack/stack-library/stack-library-selecter/che-stack-library-selecter.html';

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

  link($scope) {
    //select item
    $scope.select = () => {
      $scope.$emit('event:library:selectStackId', $scope.stackId);
    };
  }

}
