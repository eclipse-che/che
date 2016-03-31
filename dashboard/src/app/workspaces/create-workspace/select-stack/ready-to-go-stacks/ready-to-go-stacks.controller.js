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
 * This class is handling the controller for the ready-to-go stacks
 * @author Florent Benoit
 */
export class ReadyToGoStacksCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $rootScope, lodash, cheStack) {
    this.$timeout = $timeout;
    this.$rootScope = $rootScope;
    this.lodash = lodash;
    this.cheStack = cheStack;

    this.stacks = [];
    this.stack = null;

    if (cheStack.getStacks().length) {
      this.updateData();
    } else {
      let promiseStack = cheStack.fetchStacks();
      promiseStack.then(() => {
          this.updateData();
        },
        (error) => {
          // etag handling so also retrieve last data that were fetched before
          if (error.status === 304) {
            // ok
            this.updateData();
          }
        });
    }
  }

  cheSimpleSelecterDefault(stack) {
    this.stack = stack;
    this.$timeout(() => {
      this.onChange();
    });
  }

  cheSimpleSelecter(projectName, stack) {
    this.stack = stack;
    this.$timeout(() => {
      this.onChange();
    });
  }

  /**
   * Update stacks' data
   */
  updateData() {
    this.stacks.length = 0;
    var remoteStacks = this.cheStack.getStacks();
    // remote stacks are
    remoteStacks.forEach((stack) => {
      let findLink = this.lodash.find(stack.links, (link) => {
        return link.rel === 'get icon link';
      });
      if (findLink) {
        stack.iconSrc = findLink.href;
      }
      this.stacks.push(stack);
    });
    // broadcast event
    this.$rootScope.$broadcast('create-project-stacks:initialized');
  }

  /**
   * Joins the tags into a string
   */
  tagsToString(tags) {
    return tags.join(', ');
  }

}
