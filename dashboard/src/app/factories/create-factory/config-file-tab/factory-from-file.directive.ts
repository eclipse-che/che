/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

interface IFactoryFromFileScope extends ng.IScope {
  clickUpload: () => void;
}

/**
 * Defines a directive for configuring factory from file.
 * @author Oleksii Orel
 */
export class FactoryFromFile {
  private restrict: string;
  private templateUrl: string;
  private replace: boolean;
  private controller: string;
  private controllerAs: string;
  private bindToController: boolean;

  private scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/create-factory/config-file-tab/factory-from-file.html';
    this.replace = false;

    this.controller = 'FactoryFromFileCtrl';
    this.controllerAs = 'factoryFromFileCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      isImporting: '=cdvyIsImporting',
      factoryContent: '=cdvyFactoryContent'
    };
  }

  link($scope: IFactoryFromFileScope, element: ng.IAugmentedJQuery) {
    $scope.clickUpload = () => {
      // search the input fields
      let inputElements = element.find('input');
      inputElements.eq(0).click();
    };
  }

}
