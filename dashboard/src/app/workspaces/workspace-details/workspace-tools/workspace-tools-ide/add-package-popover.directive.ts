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
 * Defines the directive for popover for adding package.
 *
 * @author Oleksii Kurinnyi
 */
export class AddPackagePopover {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-tools/workspace-tools-ide/add-package-popover.html';

  bindToController: boolean = true;
  controller: string = 'AddPackagePopoverController';
  controllerAs: string = 'addPackagePopoverController';

  scope: {
    [propName: string]: string;
  } = {
    onAddPackage: '&'
  };
}
