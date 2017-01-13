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
import IdeSvc from './ide.service';
import IdeIFrameSvc from './ide-iframe/ide-iframe.service';
import {RouteHistory} from '../../components/routing/route-history.service';
import {CheWorkspace} from '../../components/api/che-workspace.factory';

/**
 * This class is handling the controller for the IDE
 * @author Florent Benoit
 */
class IdeCtrl {
  $rootScope: che.IRootScopeService;
  $routeParams: che.route.IRouteParamsService;
  $timeout: ng.ITimeoutService;
  ideSvc: IdeSvc;
  ideIFrameSvc: IdeIFrameSvc;
  cheWorkspace: CheWorkspace;

  hasData: boolean;
  workspaces: any[];
  selectedWorkspace: any = null;
  selectedWorkspaceExists: boolean = true;
  selectedWorkspaceName: string = null;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $rootScope: ng.IRootScopeService,
              $routeParams: ng.route.IRouteParamsService, $timeout: ng.ITimeoutService, ideSvc: IdeSvc,
              ideIFrameSvc: IdeIFrameSvc, cheWorkspace: CheWorkspace, routeHistory: RouteHistory) {
    this.ideSvc = ideSvc;
    this.ideIFrameSvc = ideIFrameSvc;
    this.$rootScope = <che.IRootScopeService>$rootScope;
    this.$routeParams = <che.route.IRouteParamsService>$routeParams;
    this.cheWorkspace = cheWorkspace;
    this.$timeout = $timeout;

    this.$rootScope.showIDE = false;
    this.$rootScope.wantTokeepLoader = true;

    this.selectedWorkspaceExists = true;

    // search the selected workspace
    let namespace = this.$routeParams.namespace;
    let workspace = this.$routeParams.workspaceName;
    if (!workspace) {
      this.selectedWorkspaceName = null;
    } else {
      this.selectedWorkspaceName = workspace;
    }

    let ideAction = this.$routeParams.action;
    let ideParams: any = this.$routeParams.ideParams;
    let selectedWorkspaceIdeUrl = this.cheWorkspace.getIdeUrl(namespace, this.selectedWorkspaceName);
    if (ideAction) {
      // send action
      this.ideSvc.setIDEAction(ideAction);

      // pop current route as we will redirect
      routeHistory.popCurrentPath();

      // remove action from path
      $location.url(selectedWorkspaceIdeUrl);

    } else if (ideParams) {
      let params = new Map();
      let isArray = angular.isArray(ideParams);
      if (isArray) {
        ideParams.forEach((param: string) => {
          let argParam = this.getParams(param);
          params.set(argParam.key, argParam.value);
        });
      } else {
        let argParam = this.getParams(ideParams);
        params.set(argParam.key, argParam.value);
      }

      for (let [key, val] of params) {
        this.ideSvc.setLoadingParameter(key, val);
      }

      // pop current route as we will redirect
      routeHistory.popCurrentPath();

      // remove action from path
      $location.url(selectedWorkspaceIdeUrl);

    } else {
      let promise = cheWorkspace.fetchWorkspaces();

      if (this.$routeParams.showLogs) {
        routeHistory.popCurrentPath();

        // remove action from path
        $location.url(selectedWorkspaceIdeUrl);
        $location.replace();
      }

      promise.then(() => {
        this.updateData();
      }, () => {
        this.updateData();
      });
    }

  }

  /**
   * Transform colon separator value into key/value
   * @param arg {string}
   * @returns {Object} object with key and value
   */
  getParams(arg: string): {key: string, value: string} {
    let array = arg.split(':');
    let obj: any = {};
    obj.key = array[0];
    obj.value = array[1];
    return obj;
  }

  displayIDE(): void {
    this.ideSvc.displayIDE();
  }

  updateData(): void {
    this.hasData = true;

    this.workspaces = this.cheWorkspace.getWorkspaces();
    for (let i = 0; i < this.workspaces.length; i++) {
      if (this.workspaces[i].config.name === this.selectedWorkspaceName) {
        this.selectedWorkspace = this.workspaces[i];
      }
    }

    this.selectedWorkspaceExists = !!this.selectedWorkspace;

    this.$rootScope.hideLoader = true;

    if (this.selectedWorkspace) {
      this.ideSvc.openIde(this.selectedWorkspace.id);
    }
  }
}

export default IdeCtrl;
