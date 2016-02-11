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
 * @name components.directive:cheToolbar
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-toolbar>` defines a top toolbar.
 *
 * @param {string=} che-title the title of the toolbar
 * @param {string=} che-button-name the optional name of the right button
 * @param {string=} che-button-href the optional link of the right button
 * @param {string=} che-button-href-target the optional target of the right button
 * @param {string=} che-breadcrumb-title title of the breadcrumb
 * @param {string=} che-breadcrumb-href link used by the breadcrumb
 * @param {string=} che-subheader-title title of the sub header
 * @param {string=} che-subheader-icon icon of the sub header
 *
 * @usage
 *   <che-toolbar che-title="hello"></che-toolbar>
 *
 * @example
 <example module="userDashboard">
 <file name="index.html">
 <che-toolbar che-title="Hello"
               che-button-name="My Button"
               che-button-href="http://www.eclipse.org/che"
               che-breadcrumb-title="My Breadcrumb"
               che-breadcrumb-href="http://www.eclipse.org/che"
               che-subheader-title="subtitle"
 ></che-toolbar>
 </file>
 </example>
 * @author Florent Benoit
 */
export class CheToolbar {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='E';
    this.replace = true;
    this.controller = 'CheNavBarCtrl';
    this.controllerAs = 'controller';
    this.bindToController = true;
    this.transclude= true;

  }

  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template( element, attrs){
    var title = attrs.cheTitle;
    var titleController  = attrs.cheTitleIconsController;
    var buttonHref = attrs.cheButtonHref;
    var buttonHrefTarget = attrs.cheButtonHrefTarget;
    var buttonName = attrs.cheButtonName;

    var breadcrumbTitle = attrs.cheBreadcrumbTitle;
    var breadcrumbHref = attrs.cheBreadcrumbHref;

    var subheaderTitle = attrs.cheSubheaderTitle;
    var subheaderIcon = attrs.cheSubheaderIcon;

    var theme = attrs.theme;

    if (!theme) {
      theme = 'toolbar-theme';
    }

    var template = '<div class=\"che-toolbar\"><md-toolbar md-theme=\"' + theme +'\">\n'
      + '<div layout=\"column\" flex>'
      + '<div layout=\"row\" flex class=\"che-toolbar-breadcrumb\" layout-align=\"start center\">'
      + '<button class=\"toolbar-switch\" hide-gt-md ng-click=\"controller.toggleLeftMenu()\" >'
      + '<md-icon md-font-icon=\"fa fa-bars fa-2x\"></md-icon>'
      + '</button>';

    // start href link
    if (breadcrumbHref) {
      template = template + '<a href=\"' + breadcrumbHref + '\" layout=\"row\" layout-align=\"start center\">' +
      '<i class=\"icon-breadcrumb material-design icon-ic_chevron_left_24px\" md-theme=\"default\"></i>';
    }

    if (breadcrumbTitle) {
      template = template + '<span class="che-toolbar-breadcrumb-title">' + breadcrumbTitle + '</span>';
    }

    // end href link
    if (breadcrumbHref) {
      template = template + '</a>';
    }

    template = template + '</div>'
    + '<div layout=\"row\" flex class=\"che-toolbar-header\">'
    + '<div class=\"che-toolbar-title\">'
    + '<span class=\"che-toolbar-title-label\">'
    + title + '</span><span class=\"che-toolbar-title-icons\">';
    if (titleController) {
      template = template
      + '<md-icon ng-repeat=\"icon in ' + titleController + '.toolbarIcons\" md-font-icon=\"{{icon.font}}\" ng-click=\"'
      + titleController + '.callbackToolbarClick(icon.name)\"';
    }

    template = template
    + '</span>'
    + '</div>'
    + '<span flex></span>'
    + '<div class=\"che-toolbar-button\" layout=\"row\">';

    if (buttonName) {
      template = template + '<che-button-primary che-button-title=\"' + buttonName + '\" href=\"' + buttonHref + '\"';

      if (buttonHrefTarget) {
        template = template + ' target=\"' + buttonHrefTarget + '\"';
      }

      template = template + '></che-button-primary>';
    }
    template = template + '<ng-transclude></ng-transclude>';


    template = template + '</div>'
    + '</div>'
    + '<div layout=\"row\" class=\"che-toolbar-subheader\">';
    if (subheaderIcon) {
      template = template + '<i class=\"'
      + subheaderIcon
      + '\"></i>';
    }
    if (subheaderTitle) {
      template = template
      + subheaderTitle
      + '</div>';
    }


    template = template
    + '</div>'
    + '</md-toolbar></div>';

    return template;
  }

}

