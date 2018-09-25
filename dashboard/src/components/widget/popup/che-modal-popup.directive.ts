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
 * @ngdoc directive
 * @name components.directive:cheModalPopup
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-modal-popup>` defines modal popup component as wrapper for transclude content
 *
 * @param {string=} title the title of popup massage
 * @param {Function=} on-close close popup function
 *
 * @author Oleksii Orel
 */
export class CheModalPopup {
  restrict: string;
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
    this.transclude = true;
    this.templateUrl = 'components/widget/popup/che-modal-popup.html';

    // scope values
    this.scope = {
      title: '@',
      onClose: '&'
    };
  }

}
