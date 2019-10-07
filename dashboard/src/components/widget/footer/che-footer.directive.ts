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
 * @name components.directive:cheFooter
 * @restrict E
 * @function
 * @element
 *
 * @author Ann Shumilova
 */
export class CheFooter {

  /**
   * @ngdoc directive
   * @name components.directive:cheFooter
   * @restrict E
   * @function
   * @element
   *
   * @description
   * `<che-footer>` defines a bottom footer.
   *
   * @param {string=} che-logo product logo location
   * @param {string=} che-product-name product name
   * @param {string=} che-support-email support email address
   * @param {string=} che-support-help link to support help
   *
   * @usage
   *   <che-footer che-logo="src/logo.svg"></che-footer>
   *
   * @example
   * <example module="userDashboard">
   * <file name="index.html">
   * <che-footer
   * che-logo="src/logo.svg"
   * che-product-name="Eclipse Che"
   * che-support-email="support@codenvy.com"
   * che-support-help="http://www.eclipse.org/che">
   * </che-footer>
   * </file>
   * </example>
   * @author Ann Shumilova
   */
  private restrict: string;
  private replace: boolean;
  private transclude: boolean;

  private bindToController: boolean;
  private controller: string;
  private controllerAs: string;
  private templateUrl: string;

  private scope: {
    [propName: string]: string
  };

  constructor() {
    this.templateUrl = 'components/widget/footer/che-footer.html';

    this.restrict = 'E';
    this.replace = true;
    this.transclude = true;
    this.bindToController = true;
    this.controller = 'CheFooterController';
    this.controllerAs = 'cheFooterController';

    this.scope = {
      supportHelpPath: '@cheSupportHelpPath',
      supportHelpTitle: '@cheSupportHelpTitle',
      supportEmail: '@cheSupportEmail',
      logo: '@cheLogo',
      docs: '@cheDocs',
      version: '@cheVersion',
      productName: '@cheProductName',
      links: '=cheLinks',
      email: '=?cheEmail'
    };
  }
}

