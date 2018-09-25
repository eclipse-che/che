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
 * Defines a directive for configuring factory form workspace.
 * @author Oleksii Orel
 */
export class FactoryFromWorkspace implements ng.IDirective {

  restrict = 'E';

  templateUrl = 'app/factories/create-factory/workspaces-tab/factory-from-workspace.html';
  replace = false;

  controller = 'FactoryFromWorkspaceCtrl';
  controllerAs = 'factoryFromWorkspaceCtrl';

  bindToController = true;

  // scope values
  scope = {
    isLoading: '=cdvyIsLoading',
    isImporting: '=cdvyIsImporting',
    factoryContent: '=cdvyFactoryContent'
  };

}
