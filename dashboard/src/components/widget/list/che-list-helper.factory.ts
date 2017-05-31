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
import {CheListHelper} from './che-list-helper';

/**
 * This class is handling factory for che helpers.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHelperFactory implements che.widget.ICheListHelperFactory {
  /**
   * Angular filter service
   */
  private $filter: ng.IFilterService;

  private helpers: Map<string, CheListHelper>;


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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
