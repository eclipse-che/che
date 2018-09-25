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

/**
 * @ngdoc directive
 * @name factories.directive:LastFactories
 * @description This class is handling the directive of the listing last opened factories
 * @author Oleksii Orel
 */
export class LastFactories implements ng.IDirective {
  restrict: string;
  templateUrl: string;
  replace: boolean;
  controller: string;
  controllerAs: string;
  bindToController: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/factories/last-factories/last-factories.html';
    this.replace = false;
    this.controller = 'LastFactoriesController';
    this.controllerAs = 'lastFactoriesController';
    this.bindToController = true;
  }
}
