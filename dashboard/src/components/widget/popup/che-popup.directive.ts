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
 * @ngdoc directive
 * @name components.directive:chePopup
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-popup>` defines popup component as wrapper for popup massages
 *
 * @param {string=} title the title of popup massage
 * @param {Function=} on-close close popup function
 *
 * @author Oleksii Orel
 */
export class ChePopup {
  restrict: string;
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
    this.transclude = true;
    this.templateUrl = 'components/widget/popup/che-popup.html';

    // scope values
    this.scope = {
      title: '@',
      onClose: '&'
    };
  }

}
