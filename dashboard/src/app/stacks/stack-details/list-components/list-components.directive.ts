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
 * @ngdoc directive
 * @name stacks.details.directive:listComponents
 * @restrict E
 * @element
 *
 * @description
 * `<list-components components="controller.components"
 *                   components-on-change="controller.onChange()"></list-components>` for displaying list of components
 *
 * @usage
 *   <list-components components="controller.components" components-on-change="controller.onChange()"></list-components>
 *
 * @author Oleksii Orel
 */
export class ListComponents {
  bindToController: boolean;
  restrict: string;
  templateUrl: string;
  controller: string;
  controllerAs: string;
  scope: Object;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/stacks/stack-details/list-components/list-components.html';

    this.controller = 'ListComponentsController';
    this.controllerAs = 'listComponentsController';
    this.bindToController = true;

    // scope values
    this.scope = {
      components: '=',
      componentsOnChange: '&'
    };
  }
}
