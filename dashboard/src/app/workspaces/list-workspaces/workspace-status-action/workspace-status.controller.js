/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemStatusController
 * @description This class is handling the controller for workspace status widget
 * @author Oleksii Orel
 */
export class WorkspaceStatusController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace) {
    this.cheWorkspace = cheWorkspace;

    this.isLoading = false;
  }


  startWorkspace() {
    if(this.isLoading || !this.workspace || !this.workspace.config){
      return;
    }

    this.isLoading = true;
    let promise = this.cheWorkspace.startWorkspace(this.workspace.id, this.workspace.config.defaultEnv);

    promise.then(() => {
      this.isLoading = false;
    }, () => {
      this.isLoading = false;
    });
  }

  stopWorkspace() {
    if(this.isLoading || !this.workspace){
      return;
    }

    this.isLoading = true;
    let promise = this.cheWorkspace.stopWorkspace(this.workspace.id);

    promise.then(() => {
      this.isLoading = false;
    }, () => {
      this.isLoading = false;
    });
  }

}
