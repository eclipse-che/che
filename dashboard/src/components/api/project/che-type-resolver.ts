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

import {CheProject} from '../che-project';
import {CheProjectType} from '../che-project-type';


/**
 * This class is handling resolver for project type in automatic mode.
 * @author Oleksii Orel
 */
export class CheTypeResolver {
  typesIds: Map<string, any>;

  private $q: ng.IQService;
  private cheProject: CheProject;
  private cheProjectType: CheProjectType;

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService, cheProject: CheProject, cheProjectType: CheProjectType) {
    this.$q = $q;
    this.cheProject = cheProject;
    this.cheProjectType = cheProjectType;

    this.typesIds = this.cheProjectType.getProjectTypesIDs();
  }

  /**
   * Fetch project types if it empty.
   * @returns {ng.IPromise<any>}
   */
  fetchTypes(): ng.IPromise<Map<string, any>> {
    let deferredResolve = this.$q.defer();
    if (this.typesIds.size) {
      deferredResolve.resolve();
    } else {
      return this.cheProjectType.fetchTypes().then(() => {
        deferredResolve.resolve();
      }, (error: any) => {
        deferredResolve.reject(error);
      });
    }
    return deferredResolve.promise;
  }

  /**
   * Resolve type for import project.
   * @param projectData{che.IImportProject}
   * @returns {ng.IPromise<any>}
   */
  resolveImportProjectType(projectData: che.IImportProject): ng.IPromise<any> {
    let projectDetails: che.IProject = this.getProjectDetails(projectData);
    return this.resolveProjectType(projectDetails);
  }

  /**
   * Resolve type for project.
   * @param projectDetails{che.IProject}
   * @returns {ng.IPromise<any>}
   */
  resolveProjectType(projectDetails: che.IProject): ng.IPromise<any> {
    return this.fetchTypes().then(() => {
      return this.autoCheckType(projectDetails, this.typesIds);
    }).then((projectDetails: che.IProject) => {
      return this.cheProject.updateProjectDetails(projectDetails);
    });
  }

  /**
   * Gets project details from import project object.
   * @param projectData{che.IImportProject}
   * @returns {che.IProject}
   */
  getProjectDetails(projectData: che.IImportProject): che.IProject {
    let projectDetails: che.IProject = projectData.project;
    projectDetails.source = projectData.source;
    if (!projectDetails.attributes) {
      projectDetails.attributes = {};
    }
    return projectDetails;
  }

  /**
   * Check project type and auto select one if needed.
   * @param project{che.IProject}
   * @param projectTypesByCategory{Map<string, any>}
   * @returns {ng.IPromise<any>}
   */
  autoCheckType(project: che.IProject, projectTypesByCategory: Map<string, any>): ng.IPromise<any> {
    let deferredResolve = this.$q.defer();
    let projectDetails = angular.copy(project);

    if (projectDetails.type || projectDetails.projectType) {
      projectDetails.type = projectDetails.type ? projectDetails.type : projectDetails.projectType;
      deferredResolve.resolve(projectDetails);
      return deferredResolve.promise;
    }
    this.cheProject.fetchResolve(projectDetails.name).then(() => {
      let resultResolve = this.cheProject.getResolve(projectDetails.name);
      let estimatePromises = [];
      let estimateTypes = [];

      resultResolve.forEach((sourceResolve: any) => {
        // add attributes if any
        if (sourceResolve.attributes && Object.keys(sourceResolve.attributes).length > 0) {
          for (let attributeKey in sourceResolve.attributes) {
            if (!sourceResolve.attributes.hasOwnProperty(attributeKey)) {
              continue;
            }
            projectDetails.attributes[attributeKey] = sourceResolve.attributes[attributeKey];
          }
        }
        let projectType = projectTypesByCategory.get(sourceResolve.type);
        if (projectType.primaryable) {
          // call estimate
          let estimatePromise = this.cheProject.fetchEstimate(projectDetails.name, sourceResolve.type);
          estimatePromises.push(estimatePromise);
          estimateTypes.push(sourceResolve.type);
        }
      });

      if (estimateTypes.length === 0) {
        projectDetails.type = 'blank';
        deferredResolve.resolve(projectDetails);
        return deferredResolve.promise;
      }
      // wait estimate are all finished
      let waitEstimate = this.$q.all(estimatePromises);
      let attributesByMatchingType: Map<string, any> = new Map();
      waitEstimate.then(() => {
        let firstMatchingType;
        estimateTypes.forEach((type: string) => {
          let resultEstimate = this.cheProject.getEstimate(projectDetails.name, type);
          // add attributes
          if (Object.keys(resultEstimate.attributes).length > 0) {
            attributesByMatchingType.set(type, resultEstimate.attributes);
          }
        });
        attributesByMatchingType.forEach((attributes: any, type: string) => {
          if (!firstMatchingType) {
            let projectType = projectTypesByCategory.get(type);
            if (projectType && projectType.parents) {
              projectType.parents.forEach((parentType: string) => {
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
        }
        deferredResolve.resolve(projectDetails);
      }, (error: any) => {
        deferredResolve.reject(error);
      });
    });
    return deferredResolve.promise;
  }

}
