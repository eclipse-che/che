/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
