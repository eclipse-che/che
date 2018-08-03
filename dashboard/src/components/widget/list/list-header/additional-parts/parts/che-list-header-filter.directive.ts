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

/**
 * Defines a directive for creating a filter select in list's header.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHeaderFilter implements ng.IDirective {
  restrict: string = 'E';
  scope: {
    [propName: string]: string;
  } = {
    placeholder: '@',
    searchString: '=',
    searchOnChange: '&?'
  };

  template(): string {
    return `<che-filter-selector class="che-list-header-filter"
                                 che-values="filterValues"
                                 che-on-change="onFilterChanged"></che-filter-selector>`;
  }

}
