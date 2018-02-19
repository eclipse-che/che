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

import {CheWorkspace} from '../../components/api/workspace/che-workspace.factory';

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
  isSupported(workspace: che.IWorkspace, envName?: string): boolean {
    envName = envName || workspace.config.defaultEnv;

    const supportedRecipeTypes = this.cheWorkspace.getSupportedRecipeTypes(),
      envRecipeType = workspace.config.environments[envName].recipe.type;

    return supportedRecipeTypes.indexOf(envRecipeType) !== -1;
  }

}
