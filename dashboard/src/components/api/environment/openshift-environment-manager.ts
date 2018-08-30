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
import {KubernetesEnvironmentManager} from './kubernetes-environment-manager';
import {CheRecipeTypes} from '../recipe/che-recipe-types';

/**
 * This is the implementation of environment manager that handles the openshift format.
 *
 *  @author Oleksii Orel
 */

export class OpenshiftEnvironmentManager extends KubernetesEnvironmentManager {

  constructor($log: ng.ILogService) {
    super($log);
  }

  get type(): string {
    return CheRecipeTypes.OPENSHIFT;
  }
}
