/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
