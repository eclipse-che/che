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
import {WorkspaceDetailsToolsController} from './workspace-details-tools.controller';
import {WorkspaceDetailsTools} from './workspace-details-tools.directive';
import {WorkspaceToolsIdeController} from './workspace-tools-ide/workspace-tools-ide.controller';
import {WorkspaceToolsIde} from './workspace-tools-ide/workspace-tools-ide.directive';
import {AddPackagePopoverController} from './workspace-tools-ide/add-package-popover.controller';
import {AddPackagePopover} from './workspace-tools-ide/add-package-popover.directive';


/**
 * @author Ann Shumilova
 */
export class WorkspaceToolsConfig {

  constructor(register: che.IRegisterService) {
    register.controller('WorkspaceDetailsToolsController', WorkspaceDetailsToolsController);
    register.directive('workspaceDetailsTools', WorkspaceDetailsTools);
    register.controller('WorkspaceToolsIdeController', WorkspaceToolsIdeController);
    register.directive('workspaceToolsIde', WorkspaceToolsIde);
    register.controller('AddPackagePopoverController', AddPackagePopoverController);
    register.directive('addPackagePopover', AddPackagePopover);
  }
}
