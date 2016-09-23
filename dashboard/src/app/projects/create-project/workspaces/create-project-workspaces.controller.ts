/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @ngdoc controller
 * @name projects.create-project.controller:CreateProjectWorkspacesController
 * @description This class is handling the controller for the 'select existing workspace' widget.
 * @author Oleksii Orel
 */
export class CreateProjectWorkspacesController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, cheAPI) {
    this.$timeout = $timeout;

    this.workspaces = cheAPI.getWorkspace().getWorkspaces();
  }

  /**
   * Callback when workspace has been select
   * @param workspace the workspace to use
   */
  onWorkspaceSelect(workspace) {
    this.workspace = workspace;
    this.$timeout(() => {
      this.onWorkspaceChange();
    });
  }
}
