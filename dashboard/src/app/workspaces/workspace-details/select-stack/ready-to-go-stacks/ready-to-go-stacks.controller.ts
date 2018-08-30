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
import {CheBranding} from '../../../../../components/branding/che-branding.factory';

/**
 * This class is handling the controller for the ready-to-go stacks
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ReadyToGoStacksController {

  static $inject = ['$scope', 'lodash', 'cheStack', 'cheBranding'];

  private $scope: ng.IScope;
  private lodash: any;
  private tabName: string;
  private selectedStackId: string;
  private allStackTags: Array<any> = [];
  private generalStacks: Array<che.IStack> = [];
  private filteredStackIds: Array<string> = [];
  private stackIconsMap: Map<string, string> = new Map();
  private priorityStacks: Array<string>;
  private defaultStack: string;
  private getPrioritySortPosition: any;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope,
              lodash: any,
              cheStack: CheStack,
              cheBranding: CheBranding) {
    this.$scope = $scope;
    this.lodash = lodash;
    this.priorityStacks = cheBranding.getWorkspace().priorityStacks;
    this.defaultStack = cheBranding.getWorkspace().defaultStack;

    this.getPrioritySortPosition = (stack: any) => {
      if (this.priorityStacks) {
        let sortPos = this.priorityStacks.indexOf(stack.name);
        return (sortPos > -1) ? sortPos : this.priorityStacks.length;
      } else {
        return 0;
      }
    };
    // passing priorityStacks to orderBy function:
    this.getPrioritySortPosition.priorityStacks = this.priorityStacks;

    let stacks = cheStack.getStacks();
    if (stacks.length) {
      this.updateData(stacks);
    } else {
      cheStack.fetchStacks().then(() => {
        this.updateData(stacks);
      });
    }
  }

  /**
   * Update filtered stack keys depends on tags.
   * @param tags {Array<string>}
   */
  onTagsChanges(tags?: Array<string>): void {
    if (!angular.isArray(tags) || !tags.length) {
      this.filteredStackIds = this.generalStacks.map((stack: che.IStack) => stack.id);
      this.setStackSelectionById(this.filteredStackIds[0]);
      return;
    }
    this.filteredStackIds = this.generalStacks.filter((stack: che.IStack) => {
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
   * Gets icon src.
   * @param stackId {string}
   * @returns {undefined|string}
   */
  getIconSrc(stackId: string): string {
    return this.stackIconsMap.get(stackId);
  }

  /**
   * Update stack data.
   * @param stacks {Array<che.IStack>}
   */
  updateData(stacks: Array<che.IStack>): void {
    stacks.forEach((stack: che.IStack) => {
      if (stack.scope !== 'general') {
        return;
      }
      let findLink = this.lodash.find(stack.links, (link: any) => {
        return link.rel === 'get icon link';
      });
      if (findLink) {
        this.stackIconsMap.set(stack.id, findLink.href);
      }
      this.generalStacks.push(stack);
      this.allStackTags = this.allStackTags.concat(stack.tags);
    });
    this.allStackTags = this.lodash.uniq(this.allStackTags);
    this.generalStacks.sort((stackA: che.IStack, stackB: che.IStack) => {
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

    this.onTagsChanges();
    this.selectDefaultStack();
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
   * Detects default stack and selects it.
   */
  selectDefaultStack(): void {
    if (this.defaultStack) {
      let stack = this.lodash.find(this.generalStacks, (stack: any) => {
        return stack.id === this.defaultStack;
      });
      if (stack) {
        this.setStackSelectionById(stack.id);
        return;
      }
    }

    if (this.generalStacks && this.generalStacks.length > 0) {
      this.setStackSelectionById(this.generalStacks[0].id);
    }
  }
}
