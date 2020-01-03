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

import { CheWorkspace } from '../../components/api/workspace/che-workspace.factory';

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
   * Returns `true` if supported.
   * @param {che.IWorkspace} workspace
   * @returns {boolean}
   */
  isSupported(workspace: che.IWorkspace): boolean {
    return workspace && !!workspace.devfile;
  }

}
