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
 * @name components.directive:cheBox
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-box>` defines a box used to insert data.
 *
 * @param {string=} che-title the title of the panel
 * @param {string=} che-title-icon icon font prefixing the panel's title
 * @param {string=} che-title-svg-icon path to SVG image used as panel's title
 * @param {boolean=} che-title-background ti specify the background color of the title
 *
 * @usage
 *   <che-box che-title="hello"></che-box>
 *
 * @example
 <example module="cheDashboard">
 <file name="index.html">
 <che-box che-title-icon="fa fa-lock" che-title="hello">This is simple text</che-box>
 </file>
 </example>
 * @author Florent Benoit
 */
export class CheBox {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.transclude = true;
    this.bindToController = true;


    this.controller = 'CheBoxCtrl';
    this.controllerAs = 'cheBoxCtrl';

    this.scope = {
      svgIcon: '@cheTitleSvgIcon',
      title: '@cheTitle'
    };
  }


  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template( element, attrs){
    var template = '<md-card class="che-box';
    if (attrs['cheBoxTheme']) {
      template = template + ' che-box-title-bg-' + attrs['cheBoxTheme'];
    }
    template = template + '" md-theme="default">';

    if (attrs['cheTitle']) {
      template = template + '<div layout="row" class="che-box-titlebox" layout-align="start center"><div class="che-box-title" layout="row" layout-align="start center">';


      if (attrs['cheTitleIcon']) {
        template = template + '<span class="che-box-title-icon ' + attrs['cheTitleIcon'] + '"></span>';
      }
      if (attrs['cheTitleSvgIcon']) {
        template = template + '<md-icon md-svg-src="' + '{{cheBoxCtrl.svgIcon}}' + '"></md-icon>';
      }


      template = template + '<div ng-bind-html="cheBoxCtrl.title"></div></div>'
      + '<span flex></span>';


      template = template  + '</div>';
    }
    template = template
    +  '<md-card-content class="che-box-content">'
    +  '<ng-transclude></ng-transclude>'
    +  '</md-card-content>'
    +  '</md-card>';

    return template;
  }

}
