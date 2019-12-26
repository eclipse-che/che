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
import {CheFactory} from '../../../../components/api/che-factory.factory';
import {LoadFactoryService} from '../../../factories/load-factory/load-factory.service';

/**
 * Controller for a factory item.
 * @author Oleksii Orel
 */
export class FactoryItemController {

  static $inject = ['$location', 'cheFactory', 'lodash', 'loadFactoryService'];

  private $location: ng.ILocationService;
  private cheFactory: CheFactory;
  private lodash: any;
  private factory: che.IFactory;
  private loadFactoryService: LoadFactoryService;

  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService,
              cheFactory: CheFactory,
              lodash: any,
              loadFactoryService: LoadFactoryService) {
    this.$location = $location;
    this.cheFactory = cheFactory;
    this.lodash = lodash;
    this.loadFactoryService =  loadFactoryService;
  }

  $onInit(): void { }

  /**
   * Returns `true` if supported version of factory workspace.
   * @returns {boolean}
   */
  isSupported(): boolean {
    return this.loadFactoryService.isSupported(this.factory);
  }

  /**
   * Returns the list of factory links.
   *
   * @returns {Array<any>}
   */
  getFactoryLinks(): Array<any> {
    return this.cheFactory.detectLinks(this.factory);
  }

  /**
   * Redirect to factory details.
   */
  redirectToFactoryDetails(): void {
    this.$location.path('/factory/' + this.factory.id);
  }

  /**
   * Returns display value of memory limit.
   *
   * @returns {string} display value of memory limit
   */
  getMemoryLimit(): string {
    return '-';
  }

}

