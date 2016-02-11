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

export class CheFrame {

  /**
   * Default constructor.
   */
  constructor () {
    this.restrict = 'E';
    this.transclude = true;

    // scope values
    this.scope = {
      frameText : '@cheFrameText',
      frameClass:'@cheFrameClass'
    };
  }

  template() {
    return `
      <div layout="row" layout-align="center center" layout-fill>
        <div flex="5" hide-sm hide-md hide-lg>&nbsp;</div>
        <span flex class="{{frameClass}}" ng-if="frameText && frameText.length > 0">
          {{frameText}}
        </span>
        <span class="{{frameClass}}"
              flex
              layout="row" layout-md="column" layout-sm="column"
              layout-align="center center"
              layout-wrap
              ng-if="!frameText || frameText.length < 1">
          <ng-transclude></ng-transclude>
        </span>
        <div flex="5" hide-sm hide-md hide-lg>&nbsp;</div>
      </div>`;
  }
}
