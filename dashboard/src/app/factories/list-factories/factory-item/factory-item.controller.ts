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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';

/**
 * Controller for a factory item.
 * @author Oleksii Orel
 */
export class FactoryItemController {

  static $inject = ['$location', 'cheFactory', 'cheEnvironmentRegistry', 'lodash'];

  private $location: ng.ILocationService;
  private cheFactory: CheFactory;
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  private lodash: any;
  private factory: che.IFactory;

  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService,
              cheFactory: CheFactory,
              cheEnvironmentRegistry: CheEnvironmentRegistry,
              lodash: any) {
    this.$location = $location;
    this.cheFactory = cheFactory;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.lodash = lodash;
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
    if (!this.factory.workspace || !this.factory.workspace.defaultEnv) {
      return '-';
    }

    let defaultEnvName = this.factory.workspace.defaultEnv;
    let environment = this.factory.workspace.environments[defaultEnvName];

    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    let machines = environmentManager.getMachines(environment);

    let limits = this.lodash.pluck(machines, 'attributes.memoryLimitBytes');
    let total = 0;
    limits.forEach((limit: number) => {
      if (limit) {
        total += limit / (1024 * 1024);
      }
    });

    return (total > 0) ? total + ' MB' : '-';
  }
}

