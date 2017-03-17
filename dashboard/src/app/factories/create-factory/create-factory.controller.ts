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
import {CheAPI} from '../../../components/api/che-api.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';

/**
 * Controller for a create factory.
 * @author Oleksii Orel
 * @author Florent Benoit
 */
export class CreateFactoryCtrl {
  private $location: ng.ILocationService;
  private $log: ng.ILogService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private lodash: _.LoDashStatic;
  private $filter: ng.IFilterService;
  private $document: ng.IDocumentService;
  private isLoading: boolean;
  private isImporting: boolean;
  private stackRecipeMode: string;
  private factoryContent: any;
  private factoryObject: any;
  private form: any;
  private name: string;
  private factoryId: string;
  private factoryLink: string;
  private factoryBadgeUrl: string;
  private markdown: string;


  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, cheAPI: CheAPI, $log: ng.ILogService, cheNotification: CheNotification, $scope: ng.IScope,
              $filter: ng.IFilterService, lodash: _.LoDashStatic, $document: ng.IDocumentService) {
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

    $scope.$watch('createFactoryCtrl.factoryObject', () => {
      this.factoryContent = this.$filter('json')(angular.fromJson(this.factoryObject));
    }, true);

    $scope.$watch('createFactoryCtrl.gitLocation', (newValue: string) => {
      // update underlying model
      // updating first project item
      if (!this.factoryObject) {
        let templateName = 'git';
        let promise = this.cheAPI.getFactoryTemplate().fetchFactoryTemplate(templateName);

        promise.then(() => {
          let factoryContent = this.cheAPI.getFactoryTemplate().getFactoryTemplate(templateName);
          this.factoryObject = angular.fromJson(factoryContent);
          this.updateGitProjectLocation(newValue);
        });
      } else {
        this.updateGitProjectLocation(newValue);
      }

    }, true);
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
    return this.form.$invalid;
  }

  /**
   * Update the source project location for git
   * @param location the new location
   */
  updateGitProjectLocation(location: string): void {
    let project = this.factoryObject.workspace.projects[0];
    project.source.type = 'git';
    project.source.location = location;
  }

  /**
   * Create a new factory by factory content
   * @param factoryContent
   */
  createFactoryByContent(factoryContent: any): void {
    if (!factoryContent) {
      return;
    }

    // try to set factory name
    try {
      let factoryObject = angular.fromJson(factoryContent);
      factoryObject.name = this.name;
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

      var parser = this.$document[0].createElement('a');
      parser.href = this.factoryLink;
      this.factoryId = factory.id;
      this.factoryBadgeUrl = parser.protocol + '//' + parser.hostname + '/factory/resources/codenvy-contribute.svg';

      this.markdown = '[![Contribute](' + this.factoryBadgeUrl + ')](' + this.factoryLink + ')';
    }, (error: any) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Create factory failed.');
      this.$log.error(error);
    }).then(() => {
      this.finishFlow();
    });
  }

  /*
   * Flow of creating a factory is finished, we can redirect to details of factory
   */
  finishFlow(): void {
    this.clearFactoryContent();
    this.$location.path('/factory/' + this.factoryId);
  }

}
