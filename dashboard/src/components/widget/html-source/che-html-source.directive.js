/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($sce) {
    this.$sce = $sce;
    this.restrict='E';
    //this.replace= true;
    this.transclude= true;
    this.templateUrl = 'components/widget/html-source/che-html-source.html';
  }

  link(scope, element, attributes, controller, transclude) {
    // use transclude to get the inner HTML value
    transclude(scope, (clone) => {

      // we're not using clone.text as it may remove h1, h2 for example
      var htmlValue = '';
      for (let i = 0; i < clone.length; i++) {
        htmlValue += clone[i].outerHTML  || '\n';
      }
      scope.originalContent = htmlValue;
    });
  }

}
