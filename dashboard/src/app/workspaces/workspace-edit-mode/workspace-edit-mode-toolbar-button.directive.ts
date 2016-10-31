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
 * @ngdoc directive
 * @name workspaces.details.directive:workspaceEditModeToolbarButton
 * @restrict E
 * @element
 *
 * @description
 * The `<workspace-edit-mode-toolbar-button>` directive provides a message and "Save" button.
 *
 * @param {string=} workspace-edit-mode-message message
 * @param {boolean=} workspace-edit-mode-show-message defines if message is visible
 * @callback workspace-edit-mode-on-save
 *
 * @usage
 * <workspace-edit-mode-toolbar-button
 *   workspace-edit-mode-message="ctrl.editModeMessage"
 *   workspace-edit-mode-show-message="ctrl.showMessage"
 *   workspace-edit-mode-on-save="ctrl.onSaveCallback()">
 * </workspace-edit-mode-toolbar-button>
 *
 * @author Oleksii Kurinnyi
 */
export class WorkspaceEditModeToolbarButton {
  restrict = 'E';

  replace = true;
  templateUrl = 'app/workspaces/workspace-edit-mode/workspace-edit-mode-toolbar-button.html';

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.scope = {
      message: '@?workspaceEditModeMessage',
      showMessage: '=?workspaceEditModeShowMessage',
      onSave: '&workspaceEditModeOnSave',
      disableSaveButton: '=workspaceEditDisableSaveButton'
    };
  }

}
