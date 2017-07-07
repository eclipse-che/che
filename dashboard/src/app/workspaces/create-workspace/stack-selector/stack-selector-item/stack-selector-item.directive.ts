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
 * Defines a directive for displaying stack selector's item.
 *
 * @author Oleksii Kurinnyi
 */
export class StackSelectorItem implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/stack-selector/stack-selector-item/stack-selector-item.html';
  replace: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.scope = {
      stack: '=',
      stackIconLink: '=',
      stackMachines: '=',
      stackIdSelected: '=',
      stackOnClick: '&'
    };
  }
}

