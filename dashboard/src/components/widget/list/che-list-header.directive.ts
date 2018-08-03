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
 * Defines a directive for creating header for list.
 * @author Oleksii Orel
 */
export class CheListHeader {
  restrict: string;
  replace: boolean;
  templateUrl: string;
  transclude: boolean;
  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.replace = true;
    this.transclude = true;
    this.templateUrl = 'components/widget/list/che-list-header.html';

    // scope values
    this.scope = {
      inputPlaceholder: '@?cheInputPlaceholder',
      inputValue: '=?cheSearchModel',
      onSearchChange: '&?cheOnSearchChange',
      addButtonTitle: '@?cheAddButtonTitle',
      addButtonHref: '@?cheAddButtonHref',
      onAdd: '&?cheOnAdd',
      importButtonTitle: '@?cheImportButtonTitle',
      onImport: '&?cheOnImport',
      deleteButtonTitle: '@?cheDeleteButtonTitle',
      deleteButtonDisable: '=?cheDeleteButtonDisable',
      deleteButtonDisableMessage: '@?cheDeleteButtonDisableMessage',
      onDelete: '&?cheOnDelete',
      filterValues: '=?cheFilterValues',
      onFilterChanged: '=?cheOnFilterChanged',
      hideAdd: '=?cheHideAdd',
      hideDelete: '=?cheHideDelete',
      hideSearch: '=?cheHideSearch',
      hideHeader: '=?cheHideHeader',
      hideFilter: '=?cheHideFilter'
    };

  }
}
