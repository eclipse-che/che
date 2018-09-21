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


import {WorkspacePluginsController} from './workspace-plugins.controller';
import {WorkspacePlugins} from './workspace-plugins.directive';

/**
 * @author Ann Shumilova
 */
export class WorkspacePluginsConfig {

  constructor(register: che.IRegisterService) {
    register.controller('WorkspacePluginsController', WorkspacePluginsController);
    register.directive('workspacePlugins', WorkspacePlugins);
  }
}
