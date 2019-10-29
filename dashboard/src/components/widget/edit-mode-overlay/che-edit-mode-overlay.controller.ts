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

import { ICheEditModeOverlayConfig } from './che-edit-mode-overlay.directive';
import { ConfirmDialogService } from '../../service/confirm-dialog/confirm-dialog.service';

export class CheEditModeOverlayController {

  static $inject = [
    '$location',
    '$scope',
    'confirmDialogService'
  ];
  private $location: ng.ILocationService;
  private $scope: ng.IScope;
  private confirmDialogService: ConfirmDialogService;

  private config: ICheEditModeOverlayConfig;

  constructor(
    $location: ng.ILocationService,
    $scope: ng.IScope,
    confirmDialogService: ConfirmDialogService
  ) {
    this.$location = $location;
    this.$scope = $scope;
    this.confirmDialogService = confirmDialogService;

    this.$scope.$on('$locationChangeStart', (event: ng.IAngularEvent, newUrl: string, oldUrl: string) => {
      if (this.config && this.config.preventPageLeave === false) {
        return;
      }

      // check if path remains the same
      const oldPath = this.extractPathname(oldUrl);
      const newPath = this.extractPathname(newUrl);
      if (oldPath === newPath) {
        return;
      }

      event.preventDefault();

      if (typeof this.config.onChangesDiscard !== 'function') {
        return;
      }
      return this.discardUnsavedChangesDialog().then(() => {
        return this.config.onChangesDiscard().then(() => {
          const hash = newUrl.slice(newUrl.indexOf('#') + 1, newUrl.length);
          this.$location.url(hash);
        });
      });
    });
  }

  discardUnsavedChangesDialog(): ng.IPromise<void> {
    return this.confirmDialogService.showConfirmDialog('Unsaved Changes', 'You have unsaved changes. You may go ahead and discard all changes, or close this window and save them.', { resolve: 'Discard Changes', reject: 'Cancel' });
  }


  /**
   * Returns Angular's path
   * @param url
   */
  private extractPathname(url: string): string {
      return url.slice(url.indexOf('#') + 1, url.indexOf('?') !== -1 ? url.indexOf('?') : url.length);
  }

}
