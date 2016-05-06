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
 * This class is handling the service for viewing the IDE
 * @author Florent Benoit
 */
class IdeSvc {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI, $rootScope, lodash, $mdDialog, userDashboardConfig, $timeout, $websocket, $sce, proxySettings, ideLoaderSvc, $location, routeHistory, $q, $log, cheWorkspace) {
    this.cheAPI = cheAPI;
    this.$rootScope = $rootScope;
    this.lodash = lodash;
    this.$mdDialog = $mdDialog;
    this.$timeout = $timeout;
    this.$websocket = $websocket;
    this.userDashboardConfig = userDashboardConfig;
    this.$sce = $sce;
    this.proxySettings = proxySettings;
    this.ideLoaderSvc = ideLoaderSvc;
    this.$location = $location;
    this.routeHistory = routeHistory;
    this.$q = $q;
    this.$log = $log;
    this.cheWorkspace = cheWorkspace;

    this.ideParams = new Map();

    this.currentStep = 0;
    this.lastWorkspace = null;

    this.listeningChannels = [];

    this.steps = [
      {text: 'Initializing workspace', inProgressText: 'Provision workspace and associating it with the existing user', logs: '', hasError: false},
      {text: 'Starting workspace runtime', inProgressText: 'Retrieving the stack\'s image and launching it', logs: '', hasError: false},
      {text: 'Starting workspace agent', inProgressText: 'Agents provide RESTful services like intellisense and SSH', logs: '', hasError: false},
      {text: 'Open IDE', inProgressText: 'Opening IDE', logs: '', hasError: false}
    ];

    this.preventRedirection = false;
  }

  init() {
    this.steps.forEach((step) => {
      step.logs = '';
      step.hasError = false;
    });

    if (this.lastWorkspace) {
      this.cleanupChannels(this.lastWorkspace.id);
    }
  }

  getStepText(stepNumber) {
    let entry = this.steps[stepNumber];
    if (this.currentStep >= stepNumber) {
      return entry.inProgressText;
    } else {
      return entry.text;
    }
  }

  displayIDE() {
    this.$rootScope.showIDE = true;
  }

  restoreIDE() {
    this.$rootScope.restoringIDE = true;
    this.displayIDE();
  }

  hasIdeLink() {
    return this.$rootScope.ideIframeLink && (this.$rootScope.ideIframeLink !== null);
  }

  handleError(error) {
    if (error.data.message) {
      this.steps[this.currentStep].logs += '\n' + error.data.message;
    }
    this.steps[this.currentStep].hasError = true;
    this.$log.error(error);
  }

  startIde(workspace, noIdeLoader) {
    this.lastWorkspace = workspace;

    let defer = this.$q.defer();
    if (!noIdeLoader) {
      this.ideLoaderSvc.addLoader();
    }

    this.currentStep = 1;

    let bus = this.cheAPI.getWebsocket().getBus(workspace.id);

    let startWorkspaceDefer = this.$q.defer();
    this.startWorkspace(bus, workspace).then(() => {
      this.cheWorkspace.fetchStatusChange(workspace.id, 'RUNNING').then(() => {
        return this.cheWorkspace.fetchWorkspaceDetails(workspace.id);
      }).then(() => {
        startWorkspaceDefer.resolve();
      }, (error) => {
        this.handleError(error);
        startWorkspaceDefer.reject(error);
      });
      this.cheWorkspace.fetchStatusChange(workspace.id, 'ERROR').then((data) => {
        startWorkspaceDefer.reject(data);
      });
    }, (error) => {
      startWorkspaceDefer.reject(error);
    });

    return startWorkspaceDefer.promise.then(() => {
      if (workspace.id === this.lastWorkspace.id) {
        // Now that the container is started, wait for the extension server. For this, needs to get runtime details
        let websocketUrl = this.cheWorkspace.getWebsocketUrl(workspace.id);
        // try to connect
        this.websocketReconnect = 50;
        this.connectToExtensionServer(websocketUrl, workspace.id);
      }
      return this.$q.resolve();
    }, (error) => {
      if (workspace.id === this.lastWorkspace.id) {
        this.cleanupChannels(workspace.id);
      }
      return this.$q.reject(error);
    });
  }

  startWorkspace(bus, data) {

    let startWorkspacePromise = this.cheAPI.getWorkspace().startWorkspace(data.id, data.config.defaultEnv);

    startWorkspacePromise.then((data) => {
      // get channels
      let environments = data.config.environments;
      let defaultEnvName = data.config.defaultEnv;
      let defaultEnvironment = this.lodash.find(environments, (environment) => {
        return environment.name === defaultEnvName;
      });

      let machineConfigsLinks = defaultEnvironment.machineConfigs[0].links;
      let findStatusLink = this.lodash.find(machineConfigsLinks, (machineConfigsLink) => {
        return machineConfigsLink.rel === 'get machine status channel';
      });
      let findOutputLink = this.lodash.find(machineConfigsLinks, (machineConfigsLink) => {
        return machineConfigsLink.rel === 'get machine logs channel';
      });

      let workspaceId = data.id;

      let agentChannel = 'workspace:' + data.id + ':ext-server:output';
      let statusChannel = findStatusLink ? findStatusLink.parameters[0].defaultValue : null;
      let outputChannel = findOutputLink ? findOutputLink.parameters[0].defaultValue : null;

      this.listeningChannels.push(statusChannel);
      // for now, display log of status channel in case of errors
      bus.subscribe(statusChannel, (message) => {
        if (message.eventType === 'DESTROYED' && message.workspaceId === data.id && !this.$rootScope.showIDE) {
          this.steps[this.currentStep].hasError = true;
          // need to show the error
          this.$mdDialog.show(
            this.$mdDialog.alert()
              .title('Unable to start workspace')
              .content('Unable to start workspace. It may be linked to OutOfMemory or the container has been destroyed')
              .ariaLabel('Workspace start')
              .ok('OK')
          );
        }
        if (message.eventType === 'ERROR' && message.workspaceId === data.id) {
          this.steps[this.currentStep].hasError = true;
          // need to show the error
          this.$mdDialog.show(
            this.$mdDialog.alert()
              .title('Error when starting workspace')
              .content('Unable to start workspace. Error when trying to start the workspace: ' + message.error)
              .ariaLabel('Workspace start')
              .ok('OK')
          );
        }
        this.$log.log('Status channel of workspaceID', workspaceId, message);
      });

      this.listeningChannels.push(agentChannel);
      bus.subscribe(agentChannel, (message) => {
        if (this.currentStep < 2) {
          this.currentStep = 2;
        }

        let agentStep = 2;

        if (message.eventType === 'ERROR' && message.workspaceId === data.id) {
          this.steps[agentStep].hasError = true;
          // need to show the error
          this.$mdDialog.show(
            this.$mdDialog.alert()
              .title('Error when starting agent')
              .content('Unable to start workspace agent. Error when trying to start the workspace agent: ' + message.error)
              .ariaLabel('Workspace agent start')
              .ok('OK')
          );
        }

        if (this.steps[agentStep].logs.length > 0) {
          this.steps[agentStep].logs = this.steps[agentStep].logs + '\n' + message;
        } else {
          this.steps[agentStep].logs = message;
        }
      });

      this.listeningChannels.push(outputChannel);
      bus.subscribe(outputChannel, (message) => {
        if (this.steps[this.currentStep].logs.length > 0) {
          this.steps[this.currentStep].logs = this.steps[this.currentStep].logs + '\n' + message;
        } else {
          this.steps[this.currentStep].logs = message;
        }
      });

    }, (error) => {
      this.handleError(error);
    });

    return startWorkspacePromise;
  }

  connectToExtensionServer(websocketURL, workspaceId) {
    this.currentStep = 2;
    // try to connect
    let websocketStream = this.$websocket(websocketURL);

    // on success, create project
    websocketStream.onOpen(() => {
      this.openIde(workspaceId);
      this.cleanupChannels(workspaceId, websocketStream);
    });

    // on error, retry to connect or after a delay, abort
    websocketStream.onError((error) => {
      this.websocketReconnect--;
      if (this.websocketReconnect > 0) {
        this.$timeout(() => {
          this.connectToExtensionServer(websocketURL, workspaceId);
        }, 1000);
      } else {
        this.cleanupChannels(workspaceId, websocketStream);
        this.steps[this.currentStep].hasError = true;
        this.$log.error('error when starting remote extension', error);
        // need to show the error
        this.$mdDialog.show(
          this.$mdDialog.alert()
            .title('Unable to create project')
            .content('Unable to connect to the remote extension server after workspace creation')
            .ariaLabel('Project creation')
            .ok('OK')
        );
      }
    });
  }

  setLoadingParameter(paramName, paramValue) {
    this.ideParams.set(paramName, paramValue);
  }

  setIDEAction(ideAction) {
    this.ideAction = ideAction;
  }

  openLastStartedIde(skipLoader) {
    this.openIde(this.lastWorkspace.id, skipLoader);
  }

  openIde(workspaceId, skipLoader) {
    this.$rootScope.hideNavbar = false;

    this.$timeout(() => {
      this.currentStep = 3;
    }, 0);

    if (this.$rootScope.loadingIDE === false || this.preventRedirection) {
      return;
    }

    if (skipLoader) {
      this.ideLoaderSvc.addLoader();
      this.$rootScope.hideIdeLoader = true;
    }

    let inDevMode = this.userDashboardConfig.developmentMode;
    let randVal = Math.floor((Math.random() * 1000000) + 1);
    let appendUrl = '?uid=' + randVal;

    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    this.lastWorkspace = workspace;

    let selfLink = this.getHrefLink(workspace, 'self link');
    let ideUrlLink = this.getHrefLink(workspace, 'ide url');

    if (this.ideAction != null) {
      appendUrl = appendUrl + '&action=' + this.ideAction;

      // reset action
      this.ideAction = null;
    }

    if (this.ideParams) {
      for (var [key, val] of this.ideParams) {
        appendUrl = appendUrl + '&' + key + '=' + val;
      }
      this.ideParams.clear();
    }

    if (inDevMode) {
      this.$rootScope.ideIframeLink = this.$sce.trustAsResourceUrl(ideUrlLink + appendUrl);
    } else {
      this.$rootScope.ideIframeLink = ideUrlLink + appendUrl;
    }
    if (!skipLoader) {
      this.$timeout(() => {
        this.$rootScope.hideIdeLoader = true;
      }, 4000);
    }

    this.$timeout(() => {
      this.$rootScope.showIDE = true;
      this.$rootScope.hideLoader = true;
      this.$rootScope.loadingIDE = false;
    }, 2000);
  }

  /**
   * Cleanup the websocket channels (unsubscribe)
   */
  cleanupChannels(workspaceId, websocketStream) {
    if (websocketStream != null) {
      websocketStream.close();
    }

    let workspaceBus = this.cheAPI.getWebsocket().getBus(workspaceId);

    if (workspaceBus != null) {
      this.listeningChannels.forEach((channel) => {
        workspaceBus.unsubscribe(channel);
      });
      this.listeningChannels.length = 0;
    }
  }

  /**
   * Gets link from a workspace
   * @param workspace the workspace on which analyze the links
   * @param name the name of the link to find (rel attribute)
   * @returns empty or the href attribute of the link
   */
  getHrefLink(workspace, name) {
    let links = workspace.links;
    var i = 0;
    while (i < links.length) {
      let link = links[i];
      if (link.rel === name) {
        return link.href;
      }
      i++;
    }
    return '';
  }

  setPreventRedirection(preventRedirection) {
    this.preventRedirection = preventRedirection;
  }

  getPreventRedirection() {
    return this.preventRedirection;
  }
}

export default IdeSvc;
