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

export interface ICheEditModeOverlayMessage {
  content: string;
  visible: boolean;
}

export interface ICheEditModeOverlayButton {
  action: (args?: any) => void;
  disabled?: boolean;
  name?: string;
  title?: string;
}

export interface ICheEditModeOverlayConfig {
  visible?: boolean;
  disabled?: boolean;
  message?: ICheEditModeOverlayMessage;
  saveButton?: ICheEditModeOverlayButton;
  applyButton?: ICheEditModeOverlayButton;
  cancelButton?: ICheEditModeOverlayButton;
}

export class CheEditModeOverlay implements ng.IDirective {

  restrict = 'E';

  scope = {
    config: '='
  };

  template(): string {
    return `
<div class="che-edit-mode-overlay"
     layout="row" layout-align="center center"
     ng-if="config.visible">
  <div class="che-edit-mode-overlay-message">
    <span ng-if="config.message && config.message.content && config.message.visible"
          ng-bind-html="config.message.content"></span>
  </div>

  <!-- 'Save' button -->
  <div ng-if="config.saveButton">
    <che-button-save-flat che-button-title="{{config.saveButton.title || 'Save'}}"
                          class="save-button"
                          name="{{config.saveButton.name || 'save-button'}}"
                          ng-disabled="config.disabled || config.saveButton.disabled"
                          ng-click="config.saveButton.action()"></che-button-save-flat>
  </div>

  <!-- 'Apply' button -->
  <div ng-if="config.applyButton">
    <che-button-save-flat che-button-title="{{config.applyButton.title || 'Apply'}}"
                          name="{{config.applyButton.name || 'apply-button'}}"
                          class="apply-button"
                          ng-disabled="config.disabled || config.applyButton.disabled"
                          ng-click="config.applyButton.action()"></che-button-save-flat>
  </div>

  <!-- 'Cancel' button -->
  <div ng-if="config.cancelButton">
    <che-button-cancel-flat che-button-title="{{config.cancelButton.title || 'Cancel'}}"
                            name="{{config.cancelButton.name || 'cancel-button'}}"
                            class="cancel-button"
                            ng-disabled="config.cancelButton.disabled"
                            ng-click="config.cancelButton.action()"></che-button-cancel-flat>
  </div>

</div>`;
  }

}
