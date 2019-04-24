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


import {WorkspaceEditorsController} from './workspace-editors.controller';
import {WorkspaceEditors} from './workspace-editors.directive';

/**
 * @author Ann Shumilova
 */
export class WorkspaceEditorsConfig {

  constructor(register: che.IRegisterService) {
    register.controller('WorkspaceEditorsController', WorkspaceEditorsController);
    register.directive('workspaceEditors', WorkspaceEditors);
  }
}
