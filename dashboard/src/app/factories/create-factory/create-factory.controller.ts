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
import {CheAPI} from '../../../components/api/che-api.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';

/**
 * Controller for a create factory.
 * @author Oleksii Orel
 * @author Florent Benoit
 */
export class CreateFactoryCtrl {

  static $inject = ['$location', 'cheAPI', '$log', 'cheNotification', '$scope', '$filter', 'lodash', '$document'];

  private gitLocation: string;
  private $location: ng.ILocationService;
  private $log: ng.ILogService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private lodash: any;
  private $filter: ng.IFilterService;
  private $document: ng.IDocumentService;
  private isLoading: boolean;
  private isImporting: boolean;
  private stackRecipeMode: string;
  private factoryContent: any;
  private factoryObject: any;
  private form: ng.IFormController;
  private name: string;
  private factoryId: string;
  private factoryLink: string;
  private factoryBadgeUrl: string;
  private markdown: string;


  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService,
              cheAPI: CheAPI,
              $log: ng.ILogService,
              cheNotification: CheNotification,
              $scope: ng.IScope,
              $filter: ng.IFilterService,
              lodash: any,
              $document: ng.IDocumentService) {
    this.$location = $location;
    this.cheAPI = cheAPI;
    this.$log = $log;
    this.cheNotification = cheNotification;
    this.$filter = $filter;
    this.lodash = lodash;
    this.$document = $document;

    this.isLoading = false;
    this.isImporting = false;
    this.stackRecipeMode = 'current-recipe';
    this.factoryContent = null;

    const factoryObjectWatcher = $scope.$watch(() => {
      return this.factoryObject;
    }, () => {
      this.name = this.factoryObject && this.factoryObject.name ? this.factoryObject.name : '';
      this.factoryContent = this.$filter('json')(angular.fromJson(this.factoryObject));
    }, true);
    const gitLocationWatcher = $scope.$watch(() => {
      return this.gitLocation;
    }, (newValue: string) => {
      // update underlying model
      // updating first project item
      if (!this.factoryObject) {
        let templateName = 'git';
        let factoryContent = this.cheAPI.getFactoryTemplate().getFactoryTemplate(templateName);
        this.factoryObject = angular.fromJson(factoryContent);
        this.updateGitProjectLocation(newValue);
        this.updateGitProjectName(newValue);
      } else {
        this.updateGitProjectLocation(newValue);
        this.updateGitProjectName(newValue);
      }

    }, true);
    $scope.$on('$destroy', () => {
      factoryObjectWatcher();
      gitLocationWatcher();
    });
  }

  /**
   * Clear factory content
   */
  clearFactoryContent(): void {
    this.factoryContent = null;
  }

  setForm(form: any): void {
    this.form = form;
  }

  isFormInvalid(): boolean {
    return this.form ? this.form.$invalid : false;
  }

  /**
   * Update the source project location for git
   * @param location the new location
   */
  updateGitProjectLocation(location: string): void {
    if (!this.factoryObject) {
      return;
    }
    let project = this.factoryObject.workspace.projects[0];
    project.source.type = 'git';
    project.source.location = location;
  }

  /**
   * Update the source project name and path for Git factory
   * @param location the new location
   */
  updateGitProjectName(location: string): void {
    if (!this.factoryObject || !location) {
      return;
    }

    const project = this.factoryObject.workspace.projects[0],
      re = /([^\/]+?)(?:\.git)?$/i,
      match = location.match(re);

    if (match && match[1]) {
      project.name = match[1];
      project.path = `/${match[1]}`;
    }
  }

  /**
   * Create a new factory by factory content
   * @param factoryContent
   */
  createFactoryByContent(factoryContent: any): void {
    if (!factoryContent) {
      return;
    }

    try {
      let factoryObject = angular.fromJson(factoryContent);
      if (this.name) {
        // try to set factory name
        factoryObject.name = this.name;
      }
      let projects: Array<che.IProject> = factoryObject.workspace.projects || [];
      projects.forEach((project: che.IProject) => {
        if (!project.type) {
          project.type = 'blank'
        }
      });
      factoryContent = angular.toJson(factoryObject);
    } catch (e) {
      this.$log.error(e);
    }

    this.isImporting = true;

    let promise = this.cheAPI.getFactory().createFactoryByContent(factoryContent);

    promise.then((factory: che.IFactory) => {
      this.isImporting = false;

      this.lodash.find(factory.links, (link: any) => {
        if (link.rel === 'accept' || link.rel === 'accept-named') {
          this.factoryLink = link.href;
        }
      });

      let parser = (<any>this.$document[0]).createElement('a');
      parser.href = this.factoryLink;
      this.factoryId = factory.id;
      this.factoryBadgeUrl = parser.protocol + '//' + parser.hostname + '/factory/resources/codenvy-contribute.svg';

      this.markdown = '[![Contribute](' + this.factoryBadgeUrl + ')](' + this.factoryLink + ')';
      this.finishFlow();
    }, (error: any) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Create factory failed.');
      this.$log.error(error);
    });
  }

  /**
   * Flow of creating a factory is finished, we can redirect to details of factory.
   */
  finishFlow(): void {
    this.clearFactoryContent();
    this.$location.path('/factory/' + this.factoryId);
  }
}
