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
 * @name navbar.controller:NavbarRecentWorkspacesController
 * @description This class is handling the controller of the recent workspaces to display in the navbar
 * @author Oleksii Kurinnyi
 */
export class NavbarRecentWorkspacesController {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace, ideSvc, $window, $log, $rootScope) {
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;
    this.$window = $window;
    this.$log = $log;
    this.$rootScope = $rootScope;

    // fetch workspaces when initializing
    this.cheWorkspace.fetchWorkspaces();

    this.dropdownItemTempl = [
      // running
      {
        name: 'Stop',
        scope: 'RUNNING',
        icon: 'fa fa-stop',
        _onclick: (workspaceId) => { this.stopRecentWorkspace(workspaceId) }
      },
      // stopped
      {
        name: 'Run',
        scope: 'STOPPED',
        icon: 'fa fa-play',
        _onclick: (workspaceId) => { this.runRecentWorkspace(workspaceId) }
      }
    ];
    this.dropdownItems = {};

    this.veryRecentWorkspaceId = '';
    let cleanup = $rootScope.$on('recent-workspace:set', (event, workspaceId) => {
      this.veryRecentWorkspaceId = workspaceId;
    });
    $rootScope.$on('$destroy', () => {
      cleanup();
    });
  }

  /**
   * Returns only workspaces which were opened at least once
   * @returns {*}
   */
  getRecentWorkspaces() {
    let workspaces = this.cheWorkspace.getWorkspaces();
    workspaces.forEach((workspace) => {
      if (!workspace.attributes) {
        workspace.attributes = {
          updated: 0,
          created: 0
        }
      }
      if (!workspace.attributes.updated) {
        workspace.attributes.updated = workspace.attributes.created;
      }
      if (this.veryRecentWorkspaceId === workspace.id) {
        workspace.attributes.opened = 1;
      } else {
        workspace.attributes.opened = 0;
      }
    });
    return workspaces;
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

  /**
   * Returns true if workspace is opened in IDE
   * @param workspaceId {String} workspace id
   * @returns {*|null|boolean}
   */
  isOpen(workspaceId) {
    return this.ideSvc.openedWorkspace && this.ideSvc.openedWorkspace.id === workspaceId;
  }

  /**
   * Returns IDE link
   * @param workspaceId {String} workspace id
   * @returns {string}
   */
  getIdeLink(workspaceId) {
    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    return '#/ide/' + (workspace ? (workspace.namespace + '/' + workspace.config.name) : 'unknown');
  }

  /**
   * Opens new tab/window with IDE
   * @param workspaceId {String} workspace id
   */
  openLinkInNewTab(workspaceId) {
    let url = this.getIdeLink(workspaceId);
    this.$window.open(url, '_blank');
  }

  /**
   * Builds and returns array of dropdown menu items for specified workspace
   * @param workspaceId {String} workspace id
   * @returns {*}
   */
  getDropdownItems(workspaceId) {
    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId),
      disabled = workspace && (workspace.status === 'STARTING' || workspace.status === 'STOPPING'),
      visibleScope = (workspace && (workspace.status === 'RUNNING' || workspace.status === 'STOPPING')) ? 'RUNNING' : 'STOPPED';

    if (!this.dropdownItems[workspaceId]) {
      this.dropdownItems[workspaceId] = [];
      this.dropdownItems[workspaceId] = angular.copy(this.dropdownItemTempl);
    }

    this.dropdownItems[workspaceId].forEach((item) => {
      item.disabled = disabled;
      item.hidden = item.scope !== visibleScope;
      item.onclick = () => {
        item._onclick(workspace.id)
      };
    });

    return this.dropdownItems[workspaceId];
  }

  /**
   * Stops specified workspace
   * @param workspaceId {String} workspace id
   */
  stopRecentWorkspace(workspaceId) {
    this.cheWorkspace.stopWorkspace(workspaceId).then(() => {}, (error) => {
      this.$log.error(error);
    });
  }

  /**
   * Starts specified workspace
   * @param workspaceId {String} workspace id
   */
  runRecentWorkspace(workspaceId) {
    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);

    this.updateRecentWorkspace(workspaceId);
    this.cheWorkspace.startWorkspace(workspace.id, workspace.config.defaultEnv).then(() => {}, (error) => {
      this.$log.error(error);
    });
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list
   *
   * @param workspaceId
   */
  updateRecentWorkspace(workspaceId) {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }
}
