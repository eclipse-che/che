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
 * Controller for a factory item.
 * @author Oleksii Orel
 */
export class FactoryItemCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location, cheFactory, cheEnvironmentRegistry, lodash) {
    this.$location = $location;
    this.codenvyFactory = codenvyFactory;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.lodash = lodash;
  }

  /**
   * Detect factory links.
   * @returns [string]
   */
  getFactoryLinks() {
    return this.codenvyFactory.detectLinks(this.factory);
  }

  /**
   * Redirect to factory details.
   */
  redirectToFactoryDetails() {
    this.$location.path('/factory/' + this.factory.id);
  }

  getMemoryLimit() {
    if (!this.factory.workspace) {
      return '-';
    }

    let defaultEnvName = this.factory.workspace.defaultEnv;
    let environment = this.factory.workspace.environments[defaultEnvName];

    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    let machines = environmentManager.getMachines(environment);

    let limits = this.lodash.pluck(machines, 'attributes.memoryLimitBytes');
    let total = 0;
    limits.forEach((limit) => {
      if (limit) {
        total += limit / (1024*1024);
      }
    });
    return (total > 0) ? total + ' MB' : '-';
  }
}

