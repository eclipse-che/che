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
import {CheAPI} from '../../../../components/api/che-api.factory';
import {StackController} from '../stack.controller';

/**
 * @ngdoc controller
 * @name stacks.details.controller:SelectTemplateController
 * @description This class is handling the controller for a dialog box with template projects selector for stack.
 * @author Oleksii Orel
 */
export class SelectTemplateController {
  $mdDialog: ng.material.IDialogService;
  templates: Array<che.IProject>;
  stack: che.IStack;
  selectedTemplates: Array<che.IProject>;
  callbackController: StackController;
  projectsOrderBy: string;


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI: CheAPI, $mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    this.templates = cheAPI.getProjectTemplate().getAllProjectTemplates();
    if (!this.templates.length) {
      cheAPI.getProjectTemplate().fetchTemplates();
    }

    this.projectsOrderBy = 'displayName';
    this.selectedTemplates = [];
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Helper method used to get the length of keys of the given object
   * @param projectTemplate: che.IProject
   * @param isAdd: boolean
   */
  updateSelectedTemplates(projectTemplate: che.IProject, isAdd: boolean): void {
    if (isAdd) {
      this.selectedTemplates.push(projectTemplate);
    } else {
      let index: number = this.selectedTemplates.indexOf(projectTemplate);
      if (index > -1) {
        this.selectedTemplates.splice(index, 1);
      }
    }
  }

  /**
   * Start stack test
   */
  startTest(): void {
    let stack: che.IStack = angular.copy(this.stack);
    stack.workspaceConfig.name = 'test-wksp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
    stack.workspaceConfig.projects = this.selectedTemplates;
    this.callbackController.showStackTestPopup(stack);
    this.hide();
  }

  /**
   * Helper method used to get the length of keys of the given object
   * @param items: Array<any>
   * @returns {number} - length of keys
   */
  getItemsSize(items: Array<any>): number {
    return items.length;
  }
}
