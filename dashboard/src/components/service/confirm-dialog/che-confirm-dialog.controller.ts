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

/**
 * This class is handling the controller for confirm dialog.
 *
 * @author Oleksii Orel
 */
export class CheConfirmDialogController {
  $mdDialog: ng.material.IDialogService;
  resolveButtonTitle: string;
  rejectButtonTitle: string;
  content: string;
  title: string;

  /**
   * It will hide the dialog box and reject it.
   */
  cancel(): void {
    this.$mdDialog.cancel();
  }

  /**
   * It will hide the dialog box and resolve it.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

}
