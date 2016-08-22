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
 * @author Oleksii Orel
 */
export class ReadyToGoStacksController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $rootScope, lodash, cheStack) {
    this.$timeout = $timeout;
    this.$rootScope = $rootScope;
    this.lodash = lodash;
    this.cheStack = cheStack;

    this.generalStacks = [];
    this.stacks = cheStack.getStacks();
    if (this.stacks.length) {
      this.updateData();
    } else {
      cheStack.fetchStacks().then(() => {
        this.updateData();
      });
    }
  }

  /**
   * Update stacks' data
   */
  updateData() {
    this.stacks.forEach((stack) => {
      if (stack.scope !== 'general') {
        return;
      }
      let generalStack = angular.copy(stack);
      let findLink = this.lodash.find(generalStack.links, (link) => {
        return link.rel === 'get icon link';
      });
      if (findLink) {
        generalStack.iconSrc = findLink.href;
      }
      this.generalStacks.push(generalStack);
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

  /**
   * Gets privileged stack position
   */
  getPrivilegedSortPosition(item) {
    let privilegedNames = ['Java', 'Blank'];

    let sortPos = privilegedNames.indexOf(item.name);
    if (sortPos > -1) {
      return sortPos;
    }

    return privilegedNames.length;
  }

  /**
   * Select stack
   */
  select(stack) {
    this.stack = stack;
    this.$timeout(() => {
      this.onChange();
    });
  }

  /**
   * When initializing with the first item, select it
   */
  initValue(isFirst, stack) {
    if (isFirst) {
      this.select(stack);
    }
  }

}
