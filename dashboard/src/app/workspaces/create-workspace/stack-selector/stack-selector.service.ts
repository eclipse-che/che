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

import {CheStack} from '../../../../components/api/che-stack.factory';
import {Observable} from '../../../../components/utils/observable';

/**
 * Service for stack selector.
 *
 * @author Oleksii Kurinnyi
 */
export class StackSelectorSvc extends Observable<any> {

  static $inject = ['$log', '$q', 'cheStack'];

  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Stack API interaction.
   */
  private cheStack: CheStack;
  /**
   * Selected stack ID.
   */
  private stackId: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($log: ng.ILogService, $q: ng.IQService, cheStack: CheStack) {
    super();

    this.$log = $log;
    this.$q = $q;
    this.cheStack = cheStack;
  }

  /**
   * Fetch list of available stacks.
   *
   * @return {IPromise<Array<che.IStack>>}
   */
  getOrFetchStacks(): ng.IPromise<Array<che.IStack>> {
    const stacks = this.getStacks();
    if (stacks.length) {
      return this.$q.when(stacks);
    }

    return this.cheStack.fetchStacks().then(() => {
      return this.$q.when();
    }, (error: any) => {
      if (error && error.status !== 304) {
        this.$log.error(error);
      }
      return this.$q.when();
    }).then(() => {
      const stacks = this.getStacks();
      return this.$q.when(stacks);
    });
  }

  /**
   * Sets stackId.
   *
   * @param {string} stackId
   */
  setStackId(stackId: string): void {
    this.onStackSelected(stackId);
  }

  /**
   * Callback which is called when stack is selected.
   *
   * @param {string} stackId a stack ID
   */
  onStackSelected(stackId: string): void {
    this.stackId = stackId;

    this.publish(stackId);
  }

  /**
   * Returns list of stacks.
   *
   * @return {Array<che.IStack>}
   */
  getStacks(): Array<che.IStack> {
    return this.cheStack.getStacks();
  }

  /**
   * Returns selected stack ID.
   *
   * @return {string}
   */
  getStackId(): string {
    return this.stackId;
  }

  /**
   * Returns stack by its ID.
   *
   * @param {string} stackId a stack ID
   * @return {che.IStack}
   */
  getStackById(stackId: string): che.IStack {
    return this.getStacks().find((stack: che.IStack) => {
      return stack.id === stackId;
    });
  }

}
