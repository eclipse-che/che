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
 * Defines a directive for displaying the block with source code as well (for demos).
 * @author Florent Benoit
 */
export class CheHtmlSource {
  $sce: ng.ISCEService;

  restrict: string = 'E';
  transclude: boolean = true;
  templateUrl: string = 'components/widget/html-source/che-html-source.html';

  scope: {
    [propName: string]: string
  } = {};

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($sce: ng.ISCEService) {
    this.$sce = $sce;
  }

  link(scope: ng.IScope, element: ng.IAugmentedJQuery, attributes: any, controller: ng.IControllerService, transclude: Function) {
    // use transclude to get the inner HTML value
    transclude(scope, (clone: ng.IAugmentedJQuery) => {

      // we're not using clone.text as it may remove h1, h2 for example
      let htmlValue = '';
      for (let i = 0; i < clone.length; i++) {
        htmlValue += clone[i].outerHTML  || '\n';
      }
      (<any>scope).originalContent = htmlValue;
    });
  }

}
