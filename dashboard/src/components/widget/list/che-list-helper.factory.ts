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
import {CheListHelper} from './che-list-helper';

/**
 * This class is handling factory for che helpers.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHelperFactory implements che.widget.ICheListHelperFactory {

  static $inject = ['$filter'];

  /**
   * Angular filter service
   */
  private $filter: ng.IFilterService;

  private helpers: Map<string, CheListHelper>;

  /**
   * Default constructor that is using resource
   */
  constructor($filter: ng.IFilterService) {
    this.$filter = $filter;

    this.helpers = new Map();
  }

  /**
   * Returns specified helper's instance.
   *
   * @param id {string} a string to identify helper.
   * @return {CheListHelper}
   */
  getHelper(id: string): CheListHelper {
    if (!this.helpers.has(id)) {
      this.helpers.set(id, new CheListHelper(this.$filter));
    }

    const helper = this.helpers.get(id);
    helper.clearFilters();

    return helper;
  }

  /**
   * Removes specified helper's instance.
   *
   * @param id {string} a string to identify helper.
   */
  removeHelper(id: string): void {
    this.helpers.delete(id);
  }

}
