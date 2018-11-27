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

import {EnvironmentManager} from './environment-manager';
import {CheRecipeTypes} from '../recipe/che-recipe-types';
import {DockerImageEnvironmentManager} from './docker-image-environment-manager';
import {DockerFileEnvironmentManager} from './docker-file-environment-manager';
import {ComposeEnvironmentManager} from './compose-environment-manager';
import {OpenshiftEnvironmentManager} from './openshift-environment-manager';
import {DefaultEnvironmentManager} from './default-environment-manager';
import {KubernetesEnvironmentManager} from './kubernetes-environment-manager';
import { NoEnvironmentManager } from './no-environment-manager';

export class CheEnvironmentManager {

  static $inject = ['$log'];

  private $log: ng.ILogService;

  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService) {
    this.$log = $log;
  }

  /**
   * Returns environment manager for given environment type
   *
   * @param {string} environmentType
   * @returns {EnvironmentManager}
   */
  create(environmentType: string): EnvironmentManager {
    switch (environmentType) {
      case CheRecipeTypes.DOCKERIMAGE:
        return new DockerImageEnvironmentManager(this.$log);
      case CheRecipeTypes.DOCKERFILE:
        return new DockerFileEnvironmentManager(this.$log);
      case CheRecipeTypes.COMPOSE:
        return new ComposeEnvironmentManager(this.$log);
      case CheRecipeTypes.KUBERNETES:
        return new KubernetesEnvironmentManager(this.$log);
      case CheRecipeTypes.OPENSHIFT:
        return new OpenshiftEnvironmentManager(this.$log);
      case CheRecipeTypes.NOENVIRONMENT:
        return new NoEnvironmentManager(this.$log);  
      default:
        return new DefaultEnvironmentManager(this.$log, environmentType);
    }
  }

}
