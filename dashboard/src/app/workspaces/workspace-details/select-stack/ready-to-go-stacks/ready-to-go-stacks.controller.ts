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

/**
 * This class is handling the controller for the ready-to-go stacks
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ReadyToGoStacksController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $scope, lodash, cheStack) {
    this.$timeout = $timeout;
    this.$scope = $scope;
    this.lodash = lodash;
    this.cheStack = cheStack;

    this.generalStacks = [];
    this.allStackTags = [];
    this.filteredStackIds = [];
    this.stackIconsMap = new Map();

    let stacks = cheStack.getStacks();
    if (stacks.length) {
      this.updateData(stacks);
    } else {
      cheStack.fetchStacks().then(() => {
        this.updateData(stacks);
      });
    }

    // create array of id of stacks which contain selected tags
    // to make filtration faster
    $scope.$on('event:updateFilter', (event, tags) => {
      this.allStackTags = [];
      this.filteredStackIds = [];

      if (!tags) {
        tags = [];
      }
      this.generalStacks.forEach((stack) => {
        let matches = 0,
          stackTags = stack.tags.map(tag => tag.toLowerCase());
        for (let i = 0; i < tags.length; i++) {
          if (stackTags.indexOf(tags[i].toLowerCase()) > -1) {
            matches++;
          }
        }
        if (matches === tags.length) {
          this.filteredStackIds.push(stack.id);
          this.allStackTags = this.allStackTags.concat(stack.tags);
        }
      });
      this.allStackTags = this.lodash.uniq(this.allStackTags);
    });

    // set first stack as selected after filtration finished
    $scope.$watch('filteredStacks && filteredStacks.length', (length) => {
      if (length) {
        this.setStackSelectionById($scope.filteredStacks[0].id);
      }
    });
  }

  /**
   * Update stacks' data
   * @param stacks
   */
  updateData(stacks) {
    stacks.forEach((stack) => {
      if (stack.scope !== 'general') {
        return;
      }
      let findLink = this.lodash.find(stack.links, (link) => {
        return link.rel === 'get icon link';
      });
      if (findLink) {
        this.stackIconsMap.set(stack.id, findLink.href);
      }
      this.generalStacks.push(stack);
      this.allStackTags = this.allStackTags.concat(stack.tags);
    });
    this.allStackTags = this.lodash.uniq(this.allStackTags);
  }

  /**
   * Joins the tags into a string
   * @param tags
   * @returns String
   */
  tagsToString(tags) {
    return tags.join(', ');
  }

  /**
   * Gets privileged stack position
   * @param item
   * @returns number
   */
  getPrivilegedSortPosition(item) {
    let privilegedNames = ['Java', 'Java-MySQL', 'Blank'];

    let sortPos = privilegedNames.indexOf(item.name);
    if (sortPos > -1) {
      return sortPos;
    }

    return privilegedNames.length;
  }

  /**
   * Select stack by Id
   * @param stackId
   */
  setStackSelectionById(stackId) {
    this.selectedStackId = stackId;
    if (this.selectedStackId) {
      this.$scope.$emit('event:selectStackId', {tabName: this.tabName, stackId: this.selectedStackId});
    }
  }
}
