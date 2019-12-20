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

import { CheWorkspace, WorkspaceStatus } from '../../../components/api/workspace/che-workspace.factory';
import IdeSvc from '../ide.service';
import { CheKeycloak } from '../../../components/api/che-keycloak.factory';

/*global $:false */

interface IIdeIFrameRootScope extends ng.IRootScopeService {
  showIDE: boolean;
  hideLoader: boolean;
  hideNavbar: boolean;
}


/**
 * Defines a service for displaying iframe for displaying the IDE.
 * @author Florent Benoit
 */
class IdeIFrameSvc {

  static $inject = [
    '$q',
    '$window',
    '$location',
    '$rootScope',
    '$mdSidenav',
    'cheWorkspace',
    'ideSvc',
    'cheKeycloak'
  ];

  private $q: ng.IQService;
  private $location: ng.ILocationService;
  private $rootScope: IIdeIFrameRootScope;
  private $mdSidenav: ng.material.ISidenavService;
  private cheWorkspace: CheWorkspace;
  private ideSvc: IdeSvc;
  private cheKeycloak: CheKeycloak;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $q: ng.IQService,
    $window: ng.IWindowService,
    $location: ng.ILocationService,
    $rootScope: IIdeIFrameRootScope,
    $mdSidenav: ng.material.ISidenavService,
    cheWorkspace: CheWorkspace,
    ideSvc: IdeSvc,
    cheKeycloak: CheKeycloak
  ) {
    this.$q = $q;
    this.$location = $location;
    this.$rootScope = $rootScope;
    this.$mdSidenav = $mdSidenav;
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;
    this.cheKeycloak = cheKeycloak;

    $window.addEventListener('message', (event: any) => {
      if (!event || typeof event.data !== "string") {
        return;
      }

      const msg: string = event.data;

      if ('show-ide' === msg) {
        this.showIDE();
        return;
      }

      if ('show-workspaces' === msg) {
        this.showWorkspaces();
        return;
      }

      if ('show-navbar' === msg) {
        this.showNavBar();
        return;
      }

      if ('hide-navbar' === msg) {
        this.hideNavBar();
        return;
      }

      if (msg.startsWith('restart-workspace:')) {
        this.restartWorkspace(msg);
        return;
      }

      if (msg.startsWith('update-token:')) {
        this.updateToken(msg);
        return;
      }

    }, false);
  }

  private showIDE(): void {
    if (this.isWaitingIDE()) {
      this.$rootScope.$apply(() => {
        this.$rootScope.showIDE = true;
        this.$rootScope.hideLoader = true;
      });
    }
  }

  private showWorkspaces(): void {
    this.$rootScope.$apply(() => {
      this.$location.path('/workspaces');
    });
  }

  private showNavBar(): void {
    this.$rootScope.hideNavbar = false;
    this.$mdSidenav('left').open();
  }

  private hideNavBar(): void {
    if (this.isWaitingIDE()) {
      this.$rootScope.hideNavbar = true;
      this.$mdSidenav('left').close();
    }
  }

  /**
   * Restarts the workspace
   *
   * @param message message from Editor in format
   *                restart-workspace:${workspaceId}:${token}
   *                Where
   *                  'restart-workspace' - action name
   *                  ${workspaceId} - workspace ID
   *                  ${token} - Che machine token to validate
   */
  private restartWorkspace(message: string): void {
    const [actionName, workspaceId, token] = message.split(':');

    // validate machine token if it's necessary
    const machineTokenValidationDefer = this.$q.defer<void>();
    if (this.cheKeycloak.isPresent()) {
      this.cheWorkspace.validateMachineToken(workspaceId, token).then(() => {
        machineTokenValidationDefer.resolve();
      }, () => {
        machineTokenValidationDefer.reject('Machine token is not valid.');
      });
    } else {
      machineTokenValidationDefer.resolve();
    }

    // restart the workspace
    machineTokenValidationDefer.promise.then(() => {
      this.cheWorkspace.fetchStatusChange(workspaceId, WorkspaceStatus[WorkspaceStatus.STOPPING]).then(() => {
        this.ideSvc.reloadIdeFrame();
      });

      return this.cheWorkspace.stopWorkspace(workspaceId);
    }).catch((err: any) => {
      console.error('Unable to stop workspace:', err);
    });
  }

  /**
   * Refreshes keycloak token if it is expiring in less than `validityTime` seconds.
   */
  private updateToken(msg: string): void {
    const [actionName, validityTimeStr] = msg.split(':');

    if (this.cheKeycloak.isPresent()) {
      const validityTimeMs = parseInt(validityTimeStr, 10);
      const validityTimeSec = Number.isNaN(validityTimeMs) ? 5 : Math.ceil(validityTimeMs / 1000);
      this.cheKeycloak.updateToken(validityTimeSec).catch(() => {
        console.warn('Cannot refresh keycloak token');
      });
    }
  }

  /**
   * Returns true if the user is waiting for IDE.
   * @returns {boolean}
   */
  private isWaitingIDE(): boolean {
    return /\/ide\//.test(this.$location.path());
  }

}

export default IdeIFrameSvc;
