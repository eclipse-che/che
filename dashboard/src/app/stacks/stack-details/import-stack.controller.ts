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
import {CheStack} from '../../../components/api/che-stack.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheUIElementsInjectorService} from '../../../components/service/injector/che-ui-elements-injector.service';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {ImportStackService} from './import-stack.service';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';
import {IInitData, StackController} from './stack.controller';


/**
 * Controller for import stack management.
 *
 * @author Oleksii Orel
 */
export class ImportStackController extends StackController {


  /**
   * Default constructor that is using resource injection
   */
  constructor($q: ng.IQService, $timeout: ng.ITimeoutService, $location: ng.ILocationService,
              $log: ng.ILogService, cheStack: CheStack, cheWorkspace: CheWorkspace, $mdDialog: ng.material.IDialogService,
              cheNotification: CheNotification, $document: ng.IDocumentService, cheUIElementsInjectorService: CheUIElementsInjectorService,
              $scope: ng.IScope, $window: ng.IWindowService, importStackService: ImportStackService, confirmDialogService: ConfirmDialogService, initData: IInitData) {
    super($q, $timeout, $location, $log, cheStack, cheWorkspace, $mdDialog, cheNotification, $document,
      cheUIElementsInjectorService, $scope, $window, importStackService, confirmDialogService, initData);
  };

  /**
   * Cancels stack's changes
   */
  cancelStackChanges(): void {
      this.$location.path('/stacks');
  }

  /**
   * Saves stack configuration - creates new one or updates existing.
   */
  saveStack(): void {
    const stack = angular.fromJson(this.stackJson);
    this.isLoading = true;
    this.cheStack.createStack(stack).then((stack: che.IStack) => {
      this.cheNotification.showInfo('Stack has been successfully created.');
      this.$location.path(`/stack/${stack.id}`);
    }, (error: any) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Creation stack failed.');
    }).finally(() => {
      this.isLoading = false;
    });
  }

  protected hasChanges(stack: che.IStack): boolean {
    return true;
  }
}
