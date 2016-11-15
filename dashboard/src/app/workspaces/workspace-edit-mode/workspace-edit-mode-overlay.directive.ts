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
 * @name workspaces.details.directive:workspaceEditModeOverlay
 * @restrict E
 * @element
 *
 * @description
 * The `<workspace-edit-mode-overlay>` directive is used to place the message and two buttons at the bottom of parent block.
 *
 * @param {string=} workspace-edit-mode-message message
 * @param {boolean=} workspace-edit-mode-show-message defines if message is visible
 * @callback workspace-edit-mode-on-save
 * @callback workspace-edit-mode-on-cancel
 *
 * @usage
 * <workspace-edit-mode-overlay
 *   workspace-edit-mode-message="ctrl.editModeMessage"
 *   workspace-edit-mode-show-message="ctrl.showMessage"
 *   workspace-edit-mode-on-save="ctrl.onSaveCallback()"
 *   workspace-edit-mode-on-cancel="ctrl.onCancelCallback()">
 * </workspace-edit-mode-overlay>
 *
 * @author Oleksii Kurinnyi
 */
export class WorkspaceEditModeOverlay {
  replace: boolean = true;
  transclude: boolean = true;
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-edit-mode/workspace-edit-mode-overlay.html';

  scope: {
    [propName: string]: string
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
      onCancel: '&workspaceEditModeOnCancel',
      disableSaveButton: '=workspaceEditDisableSaveButton'
    };
  }

}
