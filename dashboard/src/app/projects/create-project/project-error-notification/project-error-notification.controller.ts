/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * This class is handling the controller for the project error notification
 * @author Oleksii Orel
 */
export class ProjectErrorNotificationController {
  title: string;
  content: string;

  private $mdDialog: ng.material.IDialogService;

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  hide() {
    this.$mdDialog.hide();
  }
}
