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
 * This class is handling the service for the creation of projects
 * @author Florent Benoit
 */
export class CreateProjectSvc {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($log, $q, cheAPI) {
    this.$log = $log;
    this.$q = $q;
    this.cheAPI = cheAPI;
  }

  importProjects(workspaceId, projects, commands) {
    let promises = [];

    projects.forEach((projectData) => {
      let deferredImport = this.$q.defer();
      let deferredImportPromise = deferredImport.promise;

      let importPromise = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getProject().importProject(projectData.name, projectData.source);
      importPromise.then(() => {
        let deferredResolve = this.$q.defer();
        let deferredResolvePromise = deferredResolve.promise;

        projectData.project = projectData; // needed for resolveProjectType method only
        this.resolveProjectType(workspaceId, projectData.name, projectData, deferredResolve);
        promises.push(deferredResolvePromise);

        deferredImport.resolve();
      }, (error) => {
        deferredImport.reject(error);
      });

      promises.push(deferredImportPromise);
    });

    commands.forEach((command) => {
      let deferredAddCommand = this.$q.defer();
      let deferredAddCommandPromise = deferredAddCommand.promise;

      let importCommand = this.cheAPI.getWorkspace().addCommand(workspaceId, command);
      importCommand.then(() => {
        deferredAddCommand.resolve();
      }, (error) => {
        deferredAddCommand.reject(error);
      });

      promises.push(deferredAddCommandPromise);
    });

    return this.$q.all(promises);
  }

  importProject(workspaceId, projectData) {
    let deferredImport = this.$q.defer();
    let deferredImportPromise = deferredImport.promise;
    let deferredAddCommand = this.$q.defer();
    let deferredAddCommandPromise = deferredAddCommand.promise;
    let deferredResolve = this.$q.defer();
    let deferredResolvePromise = deferredResolve.promise;

    let importPromise = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getProject().importProject(projectData.name, projectData.source);

    importPromise.then(() => {
      // add commands if there are some that have been defined
      let commands = projectData.project.commands;
      if (commands && commands.length > 0) {
        this.addCommand(workspaceId, projectData.name, commands, 0, deferredAddCommand);
      } else {
        deferredAddCommand.resolve('no commands to add');
      }
      deferredImport.resolve();
    }, (error) => {
      deferredImport.reject(error);
    });

    // now, resolve the project
    deferredImportPromise.then(() => {
      this.resolveProjectType(workspaceId, projectData.name, projectData, deferredResolve);
    });
    return this.$q.all([deferredImportPromise, deferredAddCommandPromise, deferredResolvePromise]);
  }

  /**
   * Add commands sequentially by iterating on the number of the commands.
   * Wait the ack of remote addCommand before adding a new command to avoid concurrent access
   * @param workspaceId the ID of the workspace to use for adding commands
   * @param projectName the name that will be used to prefix the commands inserted
   * @param commands the array to follow
   * @param index the index of the array of commands to register
   */
  addCommand(workspaceId, projectName, commands, index, deferred) {
    if (index < commands.length) {
      let newCommand = angular.copy(commands[index]);

      // Update project command lines using current.project.path with actual path based on workspace runtime configuration
      // so adding the same project twice allow to use commands for each project without first selecting project in tree
      let workspace = this.cheAPI.getWorkspace().getWorkspaceById(workspaceId);
      if (workspace && workspace.runtime) {
        let runtime = workspace.runtime.devMachine.runtime;
        if (runtime) {
          let envVar = runtime.envVariables;
          if (envVar) {
            let cheProjectsRoot = envVar['CHE_PROJECTS_ROOT'];
            if (cheProjectsRoot) {
              // replace current project path by the full path of the project
              let projectPath = cheProjectsRoot + '/' + projectName;
              newCommand.commandLine = newCommand.commandLine.replace(/\$\{current.project.path\}/g, projectPath);
            }
          }
        }
      }
      newCommand.name = projectName + ': ' + newCommand.name;
      var addPromise = this.cheAPI.getWorkspace().addCommand(workspaceId, newCommand);
      addPromise.then(() => {
        // call the method again
        this.addCommand(workspaceId, projectName, commands, ++index, deferred);
      }, (error) => {
        deferred.reject(error);
      });
    } else {
      deferred.resolve('All commands added');
    }
  }

  resolveProjectType(workspaceId, projectName, projectData, deferredResolve) {
    let projectDetails = projectData.project;
    if (!projectDetails.attributes) {
      projectDetails.source = projectData.source;
      projectDetails.attributes = {};
    }

    let projectService = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getProject();
    let projectTypeService = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getProjectType();

    if (projectDetails.type) {
      let updateProjectPromise = projectService.updateProject(projectName, projectDetails);
      updateProjectPromise.then(() => {
        deferredResolve.resolve();
      }, (error) => {
        deferredResolve.reject(error);
      });
      return;
    }

    let resolvePromise = projectService.fetchResolve(projectName);
    resolvePromise.then(() => {
      let resultResolve = projectService.getResolve(projectName);
      // get project-types
      let fetchTypePromise = projectTypeService.fetchTypes();
      fetchTypePromise.then(() => {
        let projectTypesByCategory = projectTypeService.getProjectTypesIDs();

        let estimatePromises = [];
        let estimateTypes = [];
        resultResolve.forEach((sourceResolve) => {
          // add attributes if any
          if (sourceResolve.attributes && Object.keys(sourceResolve.attributes).length > 0) {
            for (let attributeKey in sourceResolve.attributes) {
              projectDetails.attributes[attributeKey] = sourceResolve.attributes[attributeKey];
            }
          }
          let projectType = projectTypesByCategory.get(sourceResolve.type);
          if (projectType.primaryable) {
            // call estimate
            let estimatePromise = projectService.fetchEstimate(projectName, sourceResolve.type);
            estimatePromises.push(estimatePromise);
            estimateTypes.push(sourceResolve.type);
          }
        });

        if (estimateTypes.length > 0) {
          // wait estimate are all finished
          let waitEstimate = this.$q.all(estimatePromises);
          let attributesByMatchingType = new Map();

          waitEstimate.then(() => {
            let firstMatchingType;
            estimateTypes.forEach((type) => {
              let resultEstimate = projectService.getEstimate(projectName, type);
              // add attributes
              if (Object.keys(resultEstimate.attributes).length > 0) {
                attributesByMatchingType.set(type, resultEstimate.attributes);
              }
            });

            attributesByMatchingType.forEach((attributes, type) => {
              if (!firstMatchingType) {
                let projectType = projectTypesByCategory.get(type);
                if (projectType && projectType.parents) {
                  projectType.parents.forEach((parentType) => {
                    if (parentType === 'java') {
                      let additionalType = 'maven';
                      if (attributesByMatchingType.get(additionalType)) {
                        firstMatchingType = additionalType;
                      }
                    }
                    if (!firstMatchingType) {
                      firstMatchingType = attributesByMatchingType.get(parentType) ? parentType : type;
                    }
                  });
                } else {
                  firstMatchingType = type;
                }
              }
            });

            if (firstMatchingType) {
              projectDetails.attributes = attributesByMatchingType.get(firstMatchingType);
              projectDetails.type = firstMatchingType;
              let updateProjectPromise = projectService.updateProject(projectName, projectDetails);
              updateProjectPromise.then(() => {
                deferredResolve.resolve();
              }, (error) => {
                this.$log.log('Update project error', projectDetails, error);
                //a second attempt with type blank
                projectDetails.attributes = {};
                projectDetails.type = 'blank';
                projectService.updateProject(projectName, projectDetails).then(() => {
                  deferredResolve.resolve();
                }, (error) => {
                  deferredResolve.reject(error);
                });
              });
            } else {
              deferredResolve.resolve();
            }
          });
        } else {
          deferredResolve.resolve();
        }
      });

    }, (error) => {
      deferredResolve.reject(error);
    });
  }
}
