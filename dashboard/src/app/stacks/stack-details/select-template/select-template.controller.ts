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
import {CheAPI} from '../../../../components/api/che-api.factory';
import {StackController} from '../stack.controller';

/**
 * @ngdoc controller
 * @name stacks.details.controller:SelectTemplateController
 * @description This class is handling the controller for a dialog box with template projects selector for stack.
 * @author Oleksii Orel
 */
export class SelectTemplateController {

  static $inject = ['cheAPI', '$mdDialog'];

  stack: che.IStack;
  selectedTemplates: Array<che.IProjectTemplate>;
  projectsOrderBy: string;

  private $mdDialog: ng.material.IDialogService;
  private templates: Array<che.IProject>;
  private callbackController: StackController;

  /**
   * Default constructor that is using resource
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
   * @param projectTemplate {che.IProjectTemplate}
   * @param isAdd {boolean}
   */
  updateSelectedTemplates(projectTemplate: che.IProjectTemplate, isAdd: boolean): void {
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
    /* tslint:disable */
    stack.workspaceConfig.name = 'test-wksp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
    /* tslint:enable */
    this.callbackController.showStackTestPopup(stack, this.selectedTemplates);
    this.hide();
  }

  /**
   * Helper method used to get the length of keys of the given object
   * @param items {Array<any>}
   * @returns {number} - length of keys
   */
  getItemsSize(items: Array<any>): number {
    return items.length;
  }
}
