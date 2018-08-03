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
import {CheStack} from '../../../../../components/api/che-stack.factory';

/**
 * This class is handling the controller for the creating stack library projects
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CreateProjectStackLibraryController {

  static $inject = ['$scope', 'cheStack', 'lodash'];

  private $scope: ng.IScope;
  private lodash: any;
  private tabName: string;
  private selectedStackId: string;
  private allStackTags: Array<string> = [];
  private filteredStackIds: Array<string> = [];
  private stacks: Array<che.IStack> = [];

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope,
              cheStack: CheStack,
              lodash: any) {
    this.$scope = $scope;
    this.lodash = lodash;

    let stacks = cheStack.getStacks();
    if (stacks.length) {
      this.updateData(stacks);
    } else {
      cheStack.fetchStacks().then(() => {
        this.updateData(stacks);
      });
    }

    $scope.$on('event:library:selectStackId', (event: ng.IAngularEvent, data: string) => {
      this.setStackSelectionById(data);
    });
  }

  /**
   * Update filtered stack keys depends on tags.
   * @param tags {Array<string>}
   */
  onTagsChanges(tags?: Array<string>): void {
    if (!angular.isArray(tags) || !tags.length) {
      this.filteredStackIds = this.stacks.map((stack: che.IStack) => stack.id);
      this.setStackSelectionById(this.filteredStackIds[0]);
      return;
    }
    this.filteredStackIds = this.stacks.filter((stack: che.IStack) => {
      let stackTags = stack.tags.map((tag: string) => tag.toLowerCase());
      return tags.every((tag: string) => {
        return stackTags.indexOf(tag.toLowerCase()) !== -1;
      });
    }).map((stack: che.IStack) => stack.id);
    if (this.filteredStackIds.length) {
      this.setStackSelectionById(this.filteredStackIds[0]);
    }
  }

  /**
   * Select stack by Id
   * @param stackId {string}
   */
  setStackSelectionById(stackId: string): void {
    this.selectedStackId = stackId;
    if (this.selectedStackId) {
      this.$scope.$emit('event:selectStackId', {tabName: this.tabName, stackId: this.selectedStackId});
    }
  }

  /**
   * Update stacks' data
   * @param stacks {Array<che.IStack>}
   */
  updateData(stacks: Array<che.IStack>): void {
    stacks.forEach((stack: che.IStack) => {
      this.filteredStackIds.push(stack.id);
      this.allStackTags = this.allStackTags.concat(stack.tags);
    });
    this.allStackTags = this.lodash.uniq(this.allStackTags);
    stacks.sort((stackA: che.IStack, stackB: che.IStack) => {
      const nameA = stackA.name.toLowerCase();
      const nameB = stackB.name.toLowerCase();
      if (nameA < nameB) {
        return -1;
      }
      if (nameA > nameB) {
        return 1;
      }
      return 0;
    });
    this.stacks = angular.copy(stacks);
    this.onTagsChanges();
  }

  /**
   * Provides tooltip data from a stack
   * @param stack {che.IStack} - the data to analyze
   * @returns {string}
   */
  getTooltip(stack: che.IStack): string {
    // get components and add data from the components
    let text = '';
    if (!stack || !stack.components) {
      return text;
    }

    stack.components.forEach((component: any) => {
      text += component.name + ':' + component.version + '   ';
    });
    return text;
  }

}
