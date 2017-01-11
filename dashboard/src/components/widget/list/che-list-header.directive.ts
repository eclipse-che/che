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
   * @ngInject for Dependency injection
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
      addButtonTitle: '@?cheAddButtonTitle',
      addButtonHref: '@?cheAddButtonHref',
      onAdd: '&?cheOnAdd',
      importButtonTitle: '@?cheImportButtonTitle',
      onImport: '&?cheOnImport',
      deleteButtonTitle: '@?cheDeleteButtonTitle',
      onDelete: '&?cheOnDelete',
      hideDelete: '=?cheHideDelete',
      hideSearch: '=?cheHideSearch',
      hideHeader: '=?cheHideHeader'
    };
  }
}
