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
 * Defines a directive for configuring factory form workspace.
 * @author Oleksii Orel
 */
export class FactoryFromWorkspace {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/create-factory/workspaces-tab/factory-from-workspace.html';
    this.replace = false;

    this.controller = 'FactoryFromWorkspaceCtrl';
    this.controllerAs = 'factoryFromWorkspaceCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      isLoading: '=cdvyIsLoading',
      isImporting: '=cdvyIsImporting',
      factoryContent: '=cdvyFactoryContent'
    };
  }

}
