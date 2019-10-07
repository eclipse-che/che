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
import {OpenShiftEnvironmentRecipeParser} from './openshift-environment-recipe-parser';
import {OpenShiftMachineRecipeParser} from './openshift-machine-recipe-parser';

/**
 * This is the implementation of environment manager that handles the openshift format.
 *
 * Format sample and specific description:
 * <code>
 * kind: List
 * items:
 * -
 *   apiVersion: v1
 *   kind: Pod
 *   metadata:
 *     name: podName
 *   spec:
 *     containers:
 *     -
 *       image: rhche/centos_jdk8:latest
 *       name: containerName
 * </code>
 *
 *
 * The recipe type is <code>openshift</code>.
 * Machines are described both in recipe and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 *
 * The OpenShift format supports everything supported in the Kubernetes format plus
 * OpenShift routes, and specifying the recipe as an OpenShift template instead of
 * a list.
 *
 *  @author Oleksii Orel
 *  @author Angel Misevski
 */

export class OpenshiftEnvironmentManager extends KubernetesEnvironmentManager {

  constructor($log: ng.ILogService) {
    super($log);

    this.parser = new OpenShiftEnvironmentRecipeParser();
    this.machineParser = new OpenShiftMachineRecipeParser();
  }

  get type(): string {
    return CheRecipeTypes.OPENSHIFT;
  }
}
