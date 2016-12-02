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
 * Defines the class for filter selector widget.
 * @author Ann Shumilova
 */
export class CheFilterSelector {

  private restrict: string = 'E';
  private bindToController: boolean = true;
  private templateUrl: string = 'components/widget/filter-selector/che-filter-selector.html';
  private controller: string = 'CheFilterSelectorController';
  private controllerAs: string = 'cheFilterSelectorController';
  private require: Array<string> = ['ngModel'];

  private scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.scope = {
      valueModel: '=ngModel',
      values: '=cheValues',
      isDisabled: '=cheDisabled',
      onChange: '=cheOnChange'
    };
  }
}
