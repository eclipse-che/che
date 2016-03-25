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
 * This class is handling the controller for the creating stack library projects
 * @author Florent Benoit
 */
export class CreateProjectStackLibraryCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, cheStack, cheWorkspace, lodash) {
    this.$scope = $scope;
    this.cheStack = cheStack;
    this.cheWorkspace = cheWorkspace;
    this.lodash = lodash;

    this.stacks = [];
    this.workspaces = [];

    this.allStackTags = [];
    this.filteredStackIds = [];

    this.onChoice();

    let promiseStack = cheStack.fetchStacks();
    promiseStack.then(() => {
        this.updateDataStacks();
      },
      (error) => {
        // etag handling so also retrieve last data that were fetched before
        if (error.status === 304) {
          // ok
          this.updateDataStacks();
        }
      });

    $scope.$on('event:selectStackId', (event, data) => {
      this.selectedStackId = data;
    });

    // create array of id of stacks which contain selected tags
    // to make filtration faster
    $scope.$on('event:updateFilter', (event, tags) => {
      this.allStackTags = [];
      this.filteredStackIds = [];

      if (!tags) {
        tags = [];
      }

      this.stacks.forEach((stack) => {
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
   * Select stack by Id
   */
  setStackSelectionById(stackId) {
    this.selectedStackId = stackId;
    this.onChoice();
  }

  /**
   * Callback when item has been select
   */
  onChoice() {
    if (!this.selectedStackId) {
      return;
    }
    this.$scope.$emit('event:selectStackId', this.selectedStackId);
  }

  /**
   * Update stacks' data
   */
  updateDataStacks() {
    this.stacks.length = 0;
    var remoteStacks = this.cheStack.getStacks();
    // remote stacks are
    remoteStacks.forEach((stack) => {
      this.stacks.push(stack);
      this.filteredStackIds.push(stack.id);
      this.allStackTags = this.allStackTags.concat(stack.tags);
    });
    this.allStackTags = this.lodash.uniq(this.allStackTags);
  }

  /**
   * Provides tooltip data from a stack
   * @param stack the data to analyze
   */
  getTooltip(stack) {
    // get components and add data from the components
    let text = '';
    stack.components.forEach((component) => {
      text += component.name + ':' + component.version + '   ';
    });
    return text;
  }

}
