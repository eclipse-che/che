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
/*global $:false */

/**
 * This class is handling the service for the creation of projects
 * @author Florent Benoit
 */
export class CreateProjectSvc {

    /**
     * Default constructor that is using resource
     * @ngInject for Dependency injection
     */
    constructor ($timeout, $compile) {
        this.$timeout = $timeout;
        this.$compile = $compile;
        this.init = false;


        this.ideAction = '';
        this.createProjectInProgress = false;

        this.currentProgressStep = 0;


        this.creationSteps = [
            {text: 'Creating and initializing workspace', inProgressText: 'Provision workspace and associating it with the existing user', logs: '', hasError: false},
            {text: 'Starting workspace runtime', inProgressText: 'Retrieving the stack\'s image and launching it', logs: '', hasError: false},
            {text: 'Starting workspace agent', inProgressText: 'Agents provide RESTful services like intellisense and SSH', logs: '', hasError: false},
            {text: 'Creating project', inProgressText: 'Creating and configuring project', logs: '', hasError: false},
            {text: 'Project created', inProgressText: 'Opening project', logs: '', hasError: false}
        ];


        this.popupVisible = false;
        this.initPopup = false;


    }


    getStepText(stepNumber) {
        let entry = this.creationSteps[stepNumber];
        if (this.currentProgressStep >= stepNumber) {
            return entry.inProgressText;
        } else {
            return entry.text;
        }
    }

    getProjectCreationSteps() {
        return this.creationSteps;
    }

    setCurrentProgressStep(currentProgressStep) {
        this.currentProgressStep = currentProgressStep;
    }

    getCurrentProgressStep() {
        return this.currentProgressStep;
    }

    hasInit() {
        return this.init;
    }

    isShowPopup() {
        return this.popupVisible;
    }

    showPopup() {
        this.popupVisible = true;
    }

    hidePopup() {
        this.popupVisible = false;
    }


    resetCreateProgress() {
        this.creationSteps.forEach((step) => {
            step.logs = '';
            step.hasError = false;
        });
        this.currentProgressStep = 0;

        this.createProjectInProgress = false;
    }


    isCreateProjectInProgress() {
        return this.createProjectInProgress;
    }

    setCreateProjectInProgress(value) {
        this.createProjectInProgress = value;
    }

    setWorkspaceOfProject(workspaceOfProject) {
        this.workspaceOfProject = workspaceOfProject;
    }

    getWorkspaceOfProject() {
        return this.workspaceOfProject;
    }

    setWorkspaceNamespace(namespace) {
      this.namespace = namespace;
    }

    getWorkspaceNamespace() {
      return this.namespace;
    }

    setProject(project) {
        this.project = project;
    }

    getProject() {
        return this.project;
    }

    createPopup() {
        if (!this.initPopup) {
            this.initPopup = true;
            // The new element to be added
            var $div = $('<create-project-popup></create-project-popup>');

            // The parent of the new element
            var $target = $('body');

            let $scope = angular.element($target).scope();
            let insertHtml = this.$compile($div)($scope);
            $target.append(insertHtml);
        }

    }


  hasIdeAction() {
    return this.getIDEAction().length > 0;
  }

  getIDEAction() {
    return this.ideAction;
  }

  setIDEAction(ideAction) {
    this.ideAction = ideAction;
  }

  getIDELink() {
    let link = '#/ide/' + this.getWorkspaceNamespace() + '/' + this.getWorkspaceOfProject();
    if (this.hasIdeAction()) {
     link = link + '?action=' + this.ideAction;
    }
    return link;
  }


}
