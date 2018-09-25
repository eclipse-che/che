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

export class CheFrame {

  restrict = 'E';
  transclude = true;

  // scope values
  scope = {
    frameText: '@cheFrameText',
    frameClass: '@cheFrameClass'
  };

  template (): string {
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
