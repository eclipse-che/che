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
 * Defines the class for filter selector widget.
 * @author Ann Shumilova
 */
export class CheFilterSelector implements ng.IDirective {

  restrict: string = 'E';
  bindToController: boolean = true;
  templateUrl: string = 'components/widget/filter-selector/che-filter-selector.html';
  controller: string = 'CheFilterSelectorController';
  controllerAs: string = 'cheFilterSelectorController';
  require: Array<string> = ['ngModel'];

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      valueModel: '=ngModel',
      values: '=cheValues',
      isDisabled: '=cheDisabled',
      onChange: '&cheOnChange',
      width: '@?cheWidth'
    };
  }
}
