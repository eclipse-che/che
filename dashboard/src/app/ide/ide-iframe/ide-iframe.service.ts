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

import { CheWorkspace } from '../../../components/api/workspace/che-workspace.factory';
import IdeSvc from '../ide.service';

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

  static $inject = ['$window', '$location', '$rootScope', '$mdSidenav', 'cheWorkspace', 'ideSvc'];

  private $location: ng.ILocationService;
  private $rootScope: IIdeIFrameRootScope;
  private $mdSidenav: ng.material.ISidenavService;
  private cheWorkspace: CheWorkspace;
  private ideSvc: IdeSvc;

  /**
   * Default constructor that is using resource
   */
  constructor($window: ng.IWindowService,
              $location: ng.ILocationService,
              $rootScope: IIdeIFrameRootScope,
              $mdSidenav: ng.material.ISidenavService,
              cheWorkspace: CheWorkspace,
              ideSvc: IdeSvc) {
    this.$location = $location;
    this.$rootScope = $rootScope;
    this.$mdSidenav = $mdSidenav;
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;

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
   *                  ${workspaceId} - workpsace ID
   *                  ${token} - Che machine token to validate
   */
  private restartWorkspace(message: string): void {
    // cut action name
    message = message.substring(message.indexOf(':') + 1);

    // get workpsace ID
    const workspaceId = message.substring(0, message.indexOf(':'));

    // get Che machine token
    const token = message.substring(message.indexOf(':') + 1);

    this.cheWorkspace.validateMachineToken(workspaceId, token).then(() => {
      this.cheWorkspace.stopWorkspace(workspaceId).then(() => {
        this.ideSvc.reloadIdeFrame();
      }).catch((error) => {
        console.error('Unable to stop workspace. ', error);
      });
    }).catch(() => {
      console.error('Unable to stop workspace: token is not valid.');
    });
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
