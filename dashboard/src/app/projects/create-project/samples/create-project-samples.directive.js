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
 * Defines a directive for creating project from samples.
 * @author Florent Benoit
 */
export class CreateProjectSamples {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout) {
    this.$timeout = $timeout;
    this.restrict='E';
    this.templateUrl = 'app/projects/create-project/samples/create-project-samples.html';


    this.controller = 'CreateProjectSamplesController';
    this.controllerAs = 'createProjectSamplesController';
    this.bindToController = true;
  }

  link($scope, element) {
    let firstTemplateName = '',
      createProjectSamplesController = $scope.createProjectSamplesController,
      createProjectCtrl = $scope.createProjectCtrl;

    $scope.$watch(() => {return createProjectCtrl.currentStackTags;}, () => {
      this.$timeout(() => {
        let firstTemplateElement = element.find('.projects-create-project-samples-list-item')[0];
        if (!firstTemplateElement || firstTemplateElement.length === 0) {
          return;
        }

        let templateName = angular.element(firstTemplateElement).data('template-name');
        if (firstTemplateName !== templateName || !createProjectSamplesController.isTemplateSelected(templateName)) {
          firstTemplateName = templateName;
          createProjectSamplesController.initItem(templateName, createProjectCtrl);
        }
      });
    });
  }
}
