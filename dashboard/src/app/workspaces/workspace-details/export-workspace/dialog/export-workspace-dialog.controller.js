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
 * @name workspace.export.controller:ExportWorkspaceDialogController
 * @description This class is handling the controller for the dialog box about the export of workspace
 * @author Florent Benoit
 */
export class ExportWorkspaceDialogController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q, $filter, lodash, cheRemote, cheNotification, $http, cheRecipeTemplate, $mdDialog, $log) {
    this.$q = $q;
    this.$filter = $filter;
    this.$http = $http;
    this.lodash = lodash;
    this.cheRemote = cheRemote;
    this.cheNotification = cheNotification;
    this.cheRecipeTemplate = cheRecipeTemplate;
    this.$mdDialog = $mdDialog;
    this.$log = $log;
    this.editorOptions = {
      lineWrapping : true,
      lineNumbers: false,
      matchBrackets: true,
      readOnly: 'nocursor',
      mode: 'application/json'
    };
    this.privateCloudUrl = '';
    this.privateCloudLogin = '';
    this.privateCloudPassword = '';
    this.retrieveWorkspace();
    this.importInProgress = false;
  }

  /**
   * It will hide the dialog box.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Gets the workspace details and links to get download the JSON file
   */
  retrieveWorkspace() {
    let copyOfWorkspace = angular.copy(this.workspaceDetails);
    this.downloadLink = '/api/workspace/' + this.workspaceId + '?downloadAsFile=' + this.workspaceDetails.name + '.json';

    //remove links
    delete copyOfWorkspace.links;
    this.exportWorkspaceContent = this.$filter('json')(angular.fromJson(copyOfWorkspace), 2);
  }

  /**
   * Start the process to export to the private Cloud
   */
  exportToPrivateCloud() {
    this.exportInCloudSteps = '';
    this.importInProgress = true;
    let login = this.cheRemote.newAuth(this.privateCloudUrl, this.privateCloudLogin, this.privateCloudPassword);

    login.then((authData) => {
      let copyOfWorkspace = angular.copy(this.workspaceDetails);
      copyOfWorkspace.config.name = 'import-' + copyOfWorkspace.config.name;

      // get content of the recipe
      let environments = copyOfWorkspace.config.environments;
      let defaultEnvName = copyOfWorkspace.config.defaultEnv;
      let defaultEnvironment = this.lodash.find(environments, (environment) => {
        return environment.name === defaultEnvName;
      });

      let recipeLocation = defaultEnvironment.machineConfigs[0].source.location;

      // get content of recipe
      this.$http.get(recipeLocation).then((response) => {

        let recipeScriptContent = response.data;
        // now upload the recipe to the remote service
        let remoteRecipeAPI = this.cheRemote.newRecipe(authData);

        let recipeContent = this.cheRecipeTemplate.getDefaultRecipe();
        recipeContent.name = 'recipe-' + copyOfWorkspace.config.name;
        recipeContent.script = recipeScriptContent;

        let createRecipePromise = remoteRecipeAPI.create(recipeContent);
        createRecipePromise.then((recipe) => {
          let findLink = this.lodash.find(recipe.links, (link) => {
            return link.rel === 'get recipe script';
          });

          // update copy of workspace with new recipe link
          let recipeLink = findLink.href;
          defaultEnvironment.machineConfigs[0].source.location = recipeLink;

          let remoteWorkspaceAPI = this.cheRemote.newWorkspace(authData);
          this.exportInCloudSteps += 'Creating remote workspace...';
          let createWorkspacePromise = remoteWorkspaceAPI.createWorkspaceFromConfig(null, copyOfWorkspace.config);
          createWorkspacePromise.then((remoteWorkspace) => {
            this.exportInCloudSteps += 'ok !<br>';

            if (remoteWorkspace.config.projects && remoteWorkspace.config.projects.length > 0) {
              this.startRemoteWorkspace(remoteWorkspaceAPI, remoteWorkspace, authData);
            } else {
              this.finishWorkspaceExporting(remoteWorkspace);
            }
          }, (error) => {
            this.handleError(error);
          })
        });
      }, (error) => {
        this.handleError(error);
      });

    }, (error) => {
      this.handleError(error);
    });
  }

  /**
   * Start remote workspace.
   *
   * @param remoteWorkspaceAPI remote workspace API
   * @param remoteWorkspace workspace to start
   * @param authData remote url and token data
   */
  startRemoteWorkspace(remoteWorkspaceAPI, remoteWorkspace, authData) {
    this.exportInCloudSteps += 'Starting remote workspace...';

    // compute WS url
    let remoteURL = authData.url;
    let remoteWsURL = remoteURL.replace('http', 'ws') + '/api/ws/';

    let startWorkspacePromise = remoteWorkspaceAPI.startWorkspace(remoteWsURL, remoteWorkspace.id, remoteWorkspace.config.defaultEnv);

    startWorkspacePromise.then((workspace) => {
      let wsAgentLink = this.lodash.find(workspace.runtime.links, (link) => {
        return link.rel === 'wsagent';
      });


      //TODO when token for wsagent is ready - will need to use it instead of wsmaster one:
      let agentAuthData = {url: wsAgentLink.href, token: authData.token};
      let remoteProjectAPI = this.cheRemote.newProject(agentAuthData);

      this.exportInCloudSteps += 'ok !<br>';
      // ok now we've to import each project with a location into the remote workspace
      let importProjectsPromise = this.importProjectsIntoWorkspace(remoteProjectAPI, remoteWorkspace);
      importProjectsPromise.then(() => {
        this.finishWorkspaceExporting(remoteWorkspace);
      }, (error) => {
        this.handleError(error);
      })
    }, (error) => {
      this.handleError(error);
    });
  }

  /**
   * Import all projects with a given source location into the remote workspace
   *
   * @param remoteProjectAPI the remote project API
   * @param workspace the remote workspace
   */
  importProjectsIntoWorkspace(remoteProjectAPI, workspace) {
    var projectPromises = [];

    workspace.config.projects.forEach((project) => {
      if (project.source && project.source.location && project.source.location.length > 0) {
        let deferred = this.$q.defer();
        let deferredPromise = deferred.promise;
        projectPromises.push(deferredPromise);
        let importProjectPromise = remoteProjectAPI.importProject(project.name, project.source);

        importProjectPromise.then(() => {
          this.exportInCloudSteps += 'Importing project ' + project.name + '...<br>';
          let updateProjectPromise = remoteProjectAPI.updateProject(project.name, project);
          updateProjectPromise.then(() => {
            deferred.resolve(workspace);
          }, (error) => {
            deferred.reject(error);
          });
        }, (error) => {
          deferred.reject(error);
        });
      }
    });
    return this.$q.all(projectPromises);
  }

  /**
   * Finilize the Workspace exporting - show proper messages, close popup.
   *
   * @param remoteWorkspace the remote exported workspace
   */
  finishWorkspaceExporting(remoteWorkspace) {
    this.exportInCloudSteps += 'Export of workspace ' + remoteWorkspace.config.name + 'finished <br>';
    this.cheNotification.showInfo('Successfully exported the workspace to ' + remoteWorkspace.config.name + ' on ' + this.privateCloudUrl);
    this.hide();
  }

  /**
   * Notify user about the error.
   * @param error the error message to display
   */
  handleError(error) {
    this.importInProgress = false;
    var message;
    if (error.data) {
      if (error.data.message) {
        message = error.data.message;
      } else {
        message = error.data;
      }
    } else {
      message = 'unable to connect to ' + error.config.url;
    }
    this.cheNotification.showError('Exporting workspace failed: ' + message);
    this.$log.error('error', message, error);
  }


}
