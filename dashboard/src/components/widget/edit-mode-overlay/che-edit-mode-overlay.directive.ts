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
  /**
   * Listeners to be called on button click.
   */
  action: (...args: any[]) => void;
  /**
   * Set field to `true` to disable button.
   */
  disabled?: boolean;
  /**
   * Button name attribute value.
   */
  name?: string;
  /**
   * Button title
   */
  title?: string;
}

export interface ICheEditModeOverlayConfig {
  /**
   * Set field to `true` to show the overlay.
   */
  visible?: boolean;
  /**
   * Set field to `true` to disable all buttons.
   */
  disabled?: boolean;
  /**
   * A message to show.
   */
  message?: ICheEditModeOverlayMessage;
  /**
   * "Save" button.
   */
  saveButton?: ICheEditModeOverlayButton;
  /**
   * "Apply" button.
   */
  applyButton?: ICheEditModeOverlayButton;
  /**
   * "Cancel" button.
   */
  cancelButton?: ICheEditModeOverlayButton;
  /**
   * If `true` then user cannot leave the pages if there are unsaved changes.
   */
  preventPageLeave?: boolean;
  /**
   * Listener to be called on `$locationChangeStart`.
   */
  onChangesDiscard?: () => ng.IPromise<void>;
}

export class CheEditModeOverlay implements ng.IDirective {

  restrict = 'E';

  bindToController = true;
  controller = 'CheEditModeOverlayController';
  controllerAs = 'cheEditModeOverlayController';

  scope = {
    config: '='
  };

  template(): string {
    return `
<div class="che-edit-mode-overlay"
     layout="row" layout-align="center center"
     ng-if="cheEditModeOverlayController.config.visible">
  <div class="che-edit-mode-overlay-message">
    <span ng-if="cheEditModeOverlayController.config.message && cheEditModeOverlayController.config.message.content && cheEditModeOverlayController.config.message.visible"
          ng-bind-html="cheEditModeOverlayController.config.message.content"></span>
  </div>

  <!-- 'Save' button -->
  <div ng-if="cheEditModeOverlayController.config.saveButton">
    <che-button-save-flat che-button-title="{{cheEditModeOverlayController.config.saveButton.title || 'Save'}}"
                          class="save-button"
                          name="{{cheEditModeOverlayController.config.saveButton.name || 'save-button'}}"
                          ng-disabled="cheEditModeOverlayController.config.disabled || cheEditModeOverlayController.config.saveButton.disabled"
                          ng-click="cheEditModeOverlayController.config.saveButton.action()"></che-button-save-flat>
  </div>

  <!-- 'Apply' button -->
  <div ng-if="cheEditModeOverlayController.config.applyButton">
    <che-button-save-flat che-button-title="{{cheEditModeOverlayController.config.applyButton.title || 'Apply'}}"
                          name="{{cheEditModeOverlayController.config.applyButton.name || 'apply-button'}}"
                          class="apply-button"
                          ng-disabled="cheEditModeOverlayController.config.disabled || cheEditModeOverlayController.config.applyButton.disabled"
                          ng-click="cheEditModeOverlayController.config.applyButton.action()"></che-button-save-flat>
  </div>

  <!-- 'Cancel' button -->
  <div ng-if="cheEditModeOverlayController.config.cancelButton">
    <che-button-cancel-flat che-button-title="{{cheEditModeOverlayController.config.cancelButton.title || 'Cancel'}}"
                            name="{{cheEditModeOverlayController.config.cancelButton.name || 'cancel-button'}}"
                            class="cancel-button"
                            ng-disabled="cheEditModeOverlayController.config.cancelButton.disabled"
                            ng-click="cheEditModeOverlayController.config.cancelButton.action()"></che-button-cancel-flat>
  </div>

</div>`;
  }

}
