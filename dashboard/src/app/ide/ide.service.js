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
    constructor (cheAPI, $rootScope, lodash, $mdDialog, userDashboardConfig, $timeout, $websocket, $sce, proxySettings, ideLoaderSvc, $location, routeHistory) {
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


        this.currentStep = 0;
        this.selectedWorkspace = null;

        this.steps = [
            {text: 'Initialize', inProgressText : 'Initializing', logs: '', hasError: false},
            {text: 'Start workspace master', inProgressText : 'Starting workspace master', logs: '', hasError: false},
            {text: 'Inject and start workspace agent', inProgressText : 'Injecting and starting workspace agent', logs: '', hasError: false},
            {text: 'View IDE', inProgressText : 'Opening IDE', logs: '', hasError: false}
        ];
    }

    init() {
        this.steps.forEach((step) => {
            step.logs = '';
            step.hasError = false;
        });

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


    setSelectedWorkspace(selectedWorkspace) {
        this.selectedWorkspace = selectedWorkspace;
    }


    startIde() {
        this.ideLoaderSvc.addLoader();

        this.currentStep = 1;

        // recipe url
        let bus = this.cheAPI.getWebsocket().getBus(this.selectedWorkspace.id);

        // subscribe to workspace events
        bus.subscribe('workspace:' + this.selectedWorkspace.id, (message) => {

            if (message.eventType === 'RUNNING' && message.workspaceId === this.selectedWorkspace.id) {

                // Now that the container is started, wait for the extension server. For this, needs to get runtime details
                let promiseRuntime = this.cheAPI.getWorkspace().fetchRuntimeConfig(this.selectedWorkspace.id);
                promiseRuntime.then(() => {
                    let websocketUrl = this.cheAPI.getWorkspace().getWebsocketUrl(this.selectedWorkspace.id);
                    // try to connect
                    this.websocketReconnect = 50;
                    this.connectToExtensionServer(websocketUrl, this.selectedWorkspace.id);

                });
            }
        });
        this.$timeout(() => {this.startWorkspace(bus, this.selectedWorkspace);}, 1000);

    }


    startWorkspace(bus, data) {

        let startWorkspacePromise = this.cheAPI.getWorkspace().startWorkspace(data.id, data.defaultEnv);

        startWorkspacePromise.then((data) => {
            // get channels
            let environments = data.environments;
            let defaultEnvName = data.defaultEnv;
            let defaultEnvironment = this.lodash.find(environments, (environment) => {
              return environment.name === defaultEnvName;
            });

            let channels = defaultEnvironment.machineConfigs[0].channels;
            let statusChannel = channels.status;
            let outputChannel = channels.output;
            let agentChannel = 'workspace:' + data.id + ':ext-server:output';


            let workspaceId = data.id;

            // for now, display log of status channel in case of errors
            bus.subscribe(statusChannel, (message) => {
                if (message.eventType === 'DESTROYED' && message.workspaceId === data.id) {
                    this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;

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
                    this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
                    // need to show the error
                    this.$mdDialog.show(
                        this.$mdDialog.alert()
                            .title('Error when starting workspace')
                            .content('Unable to start workspace. Error when trying to start the workspace: ' + message.error)
                            .ariaLabel('Workspace start')
                            .ok('OK')
                    );
                }
                console.log('Status channel of workspaceID', workspaceId, message);
            });

            bus.subscribe(agentChannel, (message) => {
              if (this.currentStep < 2) {
                this.currentStep = 2;
              }

              let agentStep = 2;
                if (this.steps[agentStep].logs.length > 0) {
                    this.steps[agentStep].logs = this.steps[agentStep].logs + '\n' + message;
                } else {
                    this.steps[agentStep].logs = message;
                }
            });

            bus.subscribe(outputChannel, (message) => {
                if (this.steps[this.currentStep].logs.length > 0) {
                    this.steps[this.currentStep].logs = this.steps[this.currentStep].logs + '\n' + message;
                } else {
                    this.steps[this.currentStep].logs = message;
                }
            });

        });
    }


    connectToExtensionServer(websocketURL, workspaceId) {
        this.currentStep = 2;
        // try to connect
        let websocketStream = this.$websocket(websocketURL);

        // on success, create project
        websocketStream.onOpen(() => {
            this.openIde();

        });

        // on error, retry to connect or after a delay, abort
        websocketStream.onError((error) => {
            this.websocketReconnect--;
            if (this.websocketReconnect > 0) {
                this.$timeout(() => {this.connectToExtensionServer(websocketURL, workspaceId);}, 1000);
            } else {
                this.steps[this.currentStep].hasError = true;
                console.log('error when starting remote extension', error);
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

    setIDEAction(ideAction) {
      this.ideAction = ideAction;
    }

    openIde(skipLoader) {
        if (skipLoader) {
            this.ideLoaderSvc.addLoader();
            this.$rootScope.hideIdeLoader = true;
        }

        this.currentStep = 3;
        let inDevMode = this.userDashboardConfig.developmentMode;
        let randVal = Math.floor((Math.random()*1000000)+1);
        let appendUrl = '?uid=' + randVal;

        let contextPath = '';
        let selfLink = this.getHrefLink(this.selectedWorkspace, 'self link');
        let ideUrlLink = this.getHrefLink(this.selectedWorkspace, 'ide url');

        if (selfLink.endsWith('ide/api/workspace/' + this.selectedWorkspace.id)) {
            contextPath = '/ide/';
        } else {
            contextPath = '/ws/';
        }

        if (this.ideAction != null) {
          appendUrl = appendUrl + '&action=' + this.ideAction;

          // reset action
          this.ideAction = null;
        }

        if (inDevMode) {
            this.$rootScope.ideIframeLink = this.$sce.trustAsResourceUrl(this.proxySettings + contextPath + this.selectedWorkspace.name + appendUrl);
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

}

export default IdeSvc;
