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
  restrict: string = 'E';
  replace: boolean = true;
  transclude: boolean = true;

  /**
   * Template for the current footer
   * @param {ng.IAugmentedJQuery} element
   * @param {any} attrs
   * @returns {string} the template
   */
  template(element: ng.IAugmentedJQuery, attrs: any) {
    let logo = attrs.cheLogo;
    let version = attrs.cheVersion;
    let productName  = attrs.cheProductName;
    let supportEmail = attrs.cheSupportEmail;
    let supportHelpPath = attrs.cheSupportHelpPath;
    let supportHelpTitle = attrs.cheSupportHelpTitle;

    let template = '<div class=\"che-footer\" layout=\"row\" layout-align=\"start center\">';
    if (logo) {
      template += '<img class=\"che-footer-logo\" ng-src=\"' + logo + '\" alt=\"logo\">';
    }
    if (version) {
      template += '<span class=\"che-footer-version\">' + version + '</span>';
    }
    template += '<div flex />';
    template += '<ng-transclude></ng-transclude>';

    // email
    if (supportEmail) {
      let subject = '?subject=' + encodeURIComponent('Wishes for ' + productName);
      template += '<a class=\"che-footer-button-blue che-footer-button\" ng-href=\"mailto:' + supportEmail + subject + '\">Make a wish<a/>';
    }

    // docs
    template += '<a class=\"che-footer-button-blue che-footer-button\" href=\"/docs\" target=\"_blank\">Docs<a/>';

    // help
    if (supportHelpPath && supportHelpTitle) {
      template += '<a class=\"che-footer-button-blue che-footer-button\" ng-href=\"' + supportHelpPath + '\" target=\"_blank\">' + supportHelpTitle + '<a/>';
    }
    template += '</div>';
    return template;
  }

}

