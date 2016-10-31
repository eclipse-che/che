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
  restrict: string;
  controller: string;
  controllerAs: string;
  replace: boolean;
  transclude: boolean;
  bindToController: boolean;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.replace = true;
    this.controller = 'CheNavBarCtrl';
    this.controllerAs = 'controller';
    this.bindToController = true;
    this.transclude = true;

  }

  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template(element, attrs) {
    var title = attrs.cheTitle;
    var titleController = attrs.cheTitleIconsController;
    var buttonHref = attrs.cheButtonHref;
    var buttonHrefTarget = attrs.cheButtonHrefTarget;
    var buttonName = attrs.cheButtonName;
    var buttonIcon = attrs.cheButtonIcon;
    var addButtonName = attrs.cheAddButtonName;
    var buttonOnClick = attrs.cheButtonOnClick;
    var buttonDisabled = attrs.cheButtonDisabled;
    var addButtonHref = attrs.cheAddButtonHref;

    var breadcrumbTitle = attrs.cheBreadcrumbTitle;
    var breadcrumbHref = attrs.cheBreadcrumbHref;

    var searchPlaceholder = attrs.cheSearchPlaceholder;
    var searchModel = attrs.cheSearchModel;

    var dropdownMenu = attrs.cheDropdownMenu;
    var id = title.replace(/[\W\s]/g, '_');
    var theme = attrs.theme;

    if (!theme) {
      theme = 'toolbar-theme';
    }

    var template = '<div class=\"che-toolbar\"><md-toolbar md-theme=\"' + theme + '\">\n'
      + '<div layout=\"row\" layout-align=\"start center\" flex>';

    if (breadcrumbHref) {
      template += '<a class=\"che-toolbar-control-button che-toolbar-breadcrumb\" href=\"' + breadcrumbHref
        + '\" title=\"' + breadcrumbTitle + '\">'
        + '<md-icon md-font-icon=\"fa fa-chevron-left\"></md-icon>'
        + '</a>';
    }

    template += '<div layout=\"row\" flex layout-align=\"start center\" class=\"che-toolbar-header\">'
      + '<div class=\"che-toolbar-title\" id=\"' + id + '\" flex layout=\"row\" layout-align=\"center center\">'
      + '<span class=\"che-toolbar-title-label\">'
      + title + '</span><span class=\"che-toolbar-title-icons\">';

    if (titleController) {
      template = template
        + '<md-icon ng-repeat=\"icon in ' + titleController + '.toolbarIcons\" md-font-icon=\"{{icon.font}}\" ng-click=\"'
        + titleController + '.callbackToolbarClick(icon.name)\"';
    }
    template += '</span></div>';

    if (searchModel) {
      template += '<che-search che-placeholder=\"' + searchPlaceholder + '\" ng-model=\"' + searchModel + '\" che-replace-element=\"' + id + '\"></che-search>';
    }

    template += '<div layout=\"row\" layout-align=\"start center\">';

    if (dropdownMenu) {
      template += '<md-menu>' + '<div class=\"che-toolbar-control-button\" ng-click=\"$mdOpenMenu($event)\">';
      template += '<md-icon md-font-icon=\"fa fa-ellipsis-v\"></md-icon></div>';
      template += '<md-menu-content width=\"3\">';
      template += '<md-menu-item ng-repeat=\"item in ' + dropdownMenu + '" >';
      template += '<md-button ng-click=\"item.onclick()\" ng-disabled=\"item.disabled()\">{{item.title}}</md-button>';
      template += '</md-menu-content></md-menu>';
    }

    if (buttonName) {
      template += '<che-button-default class=\"che-toolbar-open-button\"';
      template += ' che-button-title=\"' + buttonName + '\"';
      if (buttonDisabled) {
        template += ' ng-disabled=\"' + buttonDisabled + '\"';
      }
      if (buttonOnClick) {
        template += ' ng-click=\"' + buttonOnClick + '\"';
      }
      if (buttonIcon) {
        template += ' che-button-icon=\"' + buttonIcon + '\"';
      }
      if (buttonHref) {
        template += ' href=\"' + buttonHref + '\"';
      }
      if (buttonHrefTarget) {
        template = template + ' target=\"' + buttonHrefTarget + '\"';
      }
      template += '</che-button-default>';
    }

    if (addButtonName) {
      template += '<a class=\"che-toolbar-add-button\" title=\"' + addButtonName + '\" href=\"' + addButtonHref + '\"';
      template += '><md-icon md-font-icon=\"fa fa-plus\"></md-icon></a>';
    }

    template += '<ng-transclude></ng-transclude>';
    template += '</div></div>';
    template += '</div>'
      + '</md-toolbar></div>';

    return template;
  }

}

