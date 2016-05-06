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
 * This class is handling the controller for the IDE
 * @author Florent Benoit
 */
class IdeCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(ideSvc, $routeParams, ideLoaderSvc, ideIFrameSvc, $rootScope, cheWorkspace, $timeout, $location, routeHistory) {
    this.ideSvc = ideSvc;
    this.ideIFrameSvc = ideIFrameSvc;
    this.$rootScope = $rootScope;
    this.cheWorkspace = cheWorkspace;
    this.ideLoaderSvc = ideLoaderSvc;
    this.$timeout = $timeout;
    this.selectedWorkspace = null;
    this.$rootScope.loadingIDE = true;

    $rootScope.wantTokeepLoader = true;

    // search the selected workspace
    let workspace = $routeParams.workspaceName;
    if (!workspace) {
      this.selectedWorkspaceName = null;
    } else {
      this.selectedWorkspaceName = workspace;
    }

    let ideAction = $routeParams.action;
    let ideParams = $routeParams.ideParams;
    let selectedWorkspaceIdeUrl = this.cheWorkspace.getIdeUrl(this.selectedWorkspaceName);
    if (ideAction) {
      // send action
      this.ideSvc.setIDEAction(ideAction);

      // pop current route as we will redirect
      routeHistory.popCurrentPath();

      // remove action from path
      $location.url(selectedWorkspaceIdeUrl, false);

    } else if (ideParams) {
      let params = new Map();
      let isArray = Array.isArray(ideParams);
      if (isArray) {
        ideParams.forEach((param) => {
          let argParam = this.getParams(param);
          params.set(argParam.key, argParam.value);
        });
      } else {
        let argParam = this.getParams(ideParams);
        params.set(argParam.key, argParam.value);
      }

      for (var [key, val] of params) {
        this.ideSvc.setLoadingParameter(key, val);
      }

      // pop current route as we will redirect
      routeHistory.popCurrentPath();

      // remove action from path
      $location.url(selectedWorkspaceIdeUrl, false);

    } else {
      this.ideIFrameSvc.addIFrame();

      let promise = cheWorkspace.fetchWorkspaces();

      if ($routeParams.showLogs) {
        routeHistory.popCurrentPath();

        // remove action from path
        $location.url(selectedWorkspaceIdeUrl, false);
        $location.replace();

        this.ideSvc.setPreventRedirection($routeParams.showLogs);
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
   * @param arg
   * @returns object with key and value
   */
  getParams(arg) {
    let array = arg.split(':');
    var obj = {};
    obj.key = array[0];
    obj.value = array[1];
    return obj;
  }

  displayIDE() {
    this.ideSvc.displayIDE();
  }

  updateData() {
    this.hasData = true;

    this.workspaces = this.cheWorkspace.getWorkspaces();
    for (var i = 0; i < this.workspaces.length; i++) {
      if (this.workspaces[i].config.name === this.selectedWorkspaceName) {
        this.selectedWorkspace = this.workspaces[i];
      }
    }

    this.$rootScope.hideLoader = true;
    this.$rootScope.hideIdeLoader = true;

    if (this.selectedWorkspace) {
      this.ideSvc.setPreventRedirection(false);
      this.ideSvc.init();
      this.ideSvc.openIde(this.selectedWorkspace.id);
    }
  }
}

export default IdeCtrl;
