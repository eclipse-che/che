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
 * Defines the super class for for all buttons
 * @author Florent Benoit
 */
export class CheButton {
  restrict: string = 'E';
  bindToController: boolean = true;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {}

  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template(element: ng.IAugmentedJQuery, attrs: any) {
    let template: string = this.getTemplateStart();

    if (attrs.href) {
      template = template + ` href="${attrs.href}"`;
    }

    if (attrs.target) {
      template = template + ` target="${attrs.target}"`;
    }

    if (attrs.ngClick) {
      template = template + ` ng-click="${attrs.ngClick}"`;
    }

    if (attrs.ngHref) {
      template = template + ` ng-href="${attrs.ngHref}"`;
    }

    if (attrs.ngDisabled) {
      template = template + ` disabled="${attrs.ngHref}"`;
    }

    template = template + '>';

    if (attrs.cheButtonIcon) {
      template = template + `<md-icon md-font-icon="${attrs.cheButtonIcon}" flex layout="column" layout-align="start center"></md-icon>`;
    }


    template = template + attrs.cheButtonTitle + '</md-button>';
    return template;
  }

  getTemplateStart(): string {
    return '<md-button md-theme=\"chedefault\" class=\"che-button md-accent md-raised md-hue-2\"';
  }

  compile(element: ng.IAugmentedJQuery, attrs: any) {
    let button = element.find('button');
    if (attrs && attrs.tabindex) {
      button.attr('tabindex', attrs.tabindex);
    } else {
      button.attr('tabindex', 0);
    }
    // top level element doesn't have tabindex, only the button has
    element.attr('tabindex', -1);

    attrs.$set('ngClick', undefined);
  }

  /**
   * Re-apply ng-disabled on child
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attrs: any) {
    $scope.$watch(attrs.ngDisabled, function (isDisabled: boolean) {
      element.find('button').prop('disabled', isDisabled);
    });

  }

}
