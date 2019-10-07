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

import {CheWorkspace} from '../../components/api/workspace/che-workspace.factory';

const MINIMAL_SUPPORTED_VERSION = 7;

/**
 * This is a helper class for workspaces.
 *
 * @author Oleksii Kurinnyi
 */
export class WorkspacesService {

  static $inject = ['cheWorkspace'];

  /**
   * Workspace API interaction.
   */
  cheWorkspace: CheWorkspace;

  /**
   * Default constructor that is using resource
   */
  constructor(cheWorkspace: CheWorkspace) {
    this.cheWorkspace = cheWorkspace;
  }

  /**
   *
   * Only default environment is checked so far.
   *
   * @param {che.IWorkspace} workspace
   * @param {string=} envName environment name
   * @returns {boolean}
   */
  isSupportedRecipeType(workspace: che.IWorkspace, envName?: string): boolean {
    if (workspace.config) {
      envName = envName || workspace.config.defaultEnv;
      const supportedRecipeTypes = this.cheWorkspace.getSupportedRecipeTypes(),
        envRecipeType = envName ? workspace.config.environments[envName].recipe.type : 'no-environment';
      return supportedRecipeTypes.indexOf(envRecipeType) !== -1;
    } else if (workspace.devfile) {
      return true;
    }
  }

 /**
   *  Returns `true` if supported.
   * @param {che.IWorkspace} workspace
   * @returns {boolean}
   */
  isSupported(workspace: che.IWorkspace): boolean {
    return this.isSupportedRecipeType(workspace) && this.isSupportedVersion(workspace);
  }

   /**
   * Returns `true` if supported in the current version of the product.
   * @param {che.IWorkspace} workspace
   * @returns {boolean}
   */
  isSupportedVersion(workspace: che.IWorkspace): boolean {
    if (!workspace){
      return false;
    }
    if (workspace.devfile) {
      return true;
    }
    if (!workspace.config) {
      return false;
    }
    const config = workspace.config;

    let version: number;
    //Checking no-environment and editor or plugin attributes, that were introduced in Che7:
    if (!workspace.config.defaultEnv || (config.attributes && (config.attributes.editor || config.attributes.plugins))) {
      version = 7;
    } else {
      const machines = config.environments[config.defaultEnv].machines;
      for (const key in machines) {
        const installers = machines[key].installers;
        if (installers && installers.length !== 0) {
          version = 6;
          break;
        }
      }
    }

    return version && version >= MINIMAL_SUPPORTED_VERSION;
  }

}
