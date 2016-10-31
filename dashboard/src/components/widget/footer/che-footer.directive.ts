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
   <example module="userDashboard">
   <file name="index.html">
   <che-footer
   che-logo="src/logo.svg"
   che-product-name="Eclipse Che"
   che-support-email="support@codenvy.com"
   che-support-help="http://www.eclipse.org/che">
   </che-footer>
   </file>
   </example>
   * @author Ann Shumilova
   */
  constructor () {
    this.restrict='E';
    this.replace = true;
    this.transclude= true;
  }

  /**
   * Template for the current footer
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template(element, attrs) {
    var logo = attrs.cheLogo;
    var productName  = attrs.cheProductName;
    var supportEmail = attrs.cheSupportEmail;
    var supportHelpPath = attrs.cheSupportHelpPath;
    var supportHelpTitle = attrs.cheSupportHelpTitle;

    var template = '<div class=\"che-footer\" layout=\"row\" layout-align=\"start center\">';
    if (logo) {
      template += '<img class=\"che-footer-logo\" ng-src=\"' + logo + '\" alt=\"logo\">';
    }
    template += '<div flex />';
    template += '<ng-transclude></ng-transclude>';

    if (supportEmail) {
      let subject = '?subject=' + encodeURIComponent('Wishes for ' + productName);
      template += '<input class=\"che-footer-input\" ng-model=\"productWish\" placeholder=\"I wish this product...\" />';
      template += '<a class=\"che-footer-button-mail che-footer-button\" ng-href=\"mailto:' + supportEmail + subject + '&body={{productWish}}\">Make a wish<a/>';
    }

    if (supportHelpPath && supportHelpTitle) {
      template += '<a class=\"che-footer-button-community che-footer-button\" ng-href=\"' + supportHelpPath + '\" target=\"_blank\">' + supportHelpTitle + '<a/>';
    }
    template += '</div>';
    return template;
  }

}

