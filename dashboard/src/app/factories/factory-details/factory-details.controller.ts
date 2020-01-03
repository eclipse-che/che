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
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheFactory} from '../../../components/api/che-factory.factory';
import {LoadFactoryService} from '../../factories/load-factory/load-factory.service';

/**
 * Controller for a factory details.
 * @author Florent Benoit
 */
export class FactoryDetailsController {

  static $inject = ['$route', 'cheFactory', 'cheNotification', 'loadFactoryService'];

  private cheFactory: CheFactory;
  private factory: che.IFactory;
  private loadFactoryService: LoadFactoryService;

  /**
   * Default constructor that is using resource injection
   */
  constructor($route: ng.route.IRouteService, cheFactory: CheFactory, cheNotification: CheNotification, loadFactoryService: LoadFactoryService) {

    this.cheFactory = cheFactory;
    let factoryId = $route.current.params.id;
    this.factory = this.cheFactory.getFactoryById(factoryId);
    this.loadFactoryService = loadFactoryService;

    cheFactory.fetchFactoryById(factoryId).then((factory: che.IFactory) => {
      this.factory = factory;
    }, (error: any) => {
      cheNotification.showError(error.data.message ? error.data.message : 'Get factory failed.');
    });
  }

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  /**
   * Returns `true` if supported version of factory workspace.
   * @returns {boolean}
   */
  isSupported(): boolean {
    return this.loadFactoryService.isSupported(this.factory);
  }

  /**
   * Returns the factory url based on id.
   * @returns {link.href|*} link value
   */
  getFactoryIdUrl(): string {
    if (!this.factory) {
      return null;
    }
    return this.cheFactory.getFactoryIdUrl(this.factory);
  }
}

