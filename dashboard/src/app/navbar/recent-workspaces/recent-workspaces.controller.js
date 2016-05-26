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
 * @name navbar.controller:NavbarLastWorkspacesController
 * @description This class is handling the controller of the recent workspaces to display in the navbar
 * @author Oleksii Kurinnyi
 */
export class NavbarRecentWorkspacesCtrl {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace, lodash, ideSvc, $window) {
    this.cheWorkspace = cheWorkspace;
    this.lodash = lodash;
    this.ideSvc = ideSvc;
    this.$window = $window;

    // fetch workspaces when initializing
    this.cheWorkspace.fetchWorkspaces();
  }

  /**
   * Returns only workspaces which were opened at least once
   * @returns {*}
   */
  getRecentWorkspaces() {
    return this.lodash.filter(this.cheWorkspace.getWorkspaces(), (workspace) => {
      if (!workspace.attributes) {
        return false;
      }
      if (!workspace.attributes.updated) {
        workspace.attributes.updated = workspace.attributes.created;
      }
      return workspace.attributes.updated;
    });
  }

  /**
   * Returns status of workspace
   * @returns {String}
   */
  getWorkspaceStatus(workspaceId) {
    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    return workspace ? workspace.status : 'unknown';
  }

  /**
   * Returns name of workspace
   * @returns {String}
   */
  getWorkspaceName(workspaceId) {
    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    return workspace ? workspace.config.name : 'unknown';
  }

  isOpen(workspaceId) {
    return this.ideSvc.lastWorkspace && this.ideSvc.lastWorkspace.id === workspaceId;
  }

  getIdeLink(workspaceId) {
    return '#/ide/' + this.getWorkspaceName(workspaceId);
  }

  openLinkInNewTab(workspaceId) {
    let url = this.getIdeLink(workspaceId);
    this.$window.open(url, '_blank');
  }
}
