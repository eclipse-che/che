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

export interface ICheButtonAttributes extends ng.IAttributes {
  cheButtonIcon: string;
  cheButtonTitle: string;
  href: string;
  target: string;
  tabindex: string;
  ngHref: string;
  ngClick: string;
  ngDisabled: string;
}

/**
 * Defines the super class for for all buttons
 * @author Florent Benoit
 */
export abstract class CheButton implements ng.IDirective {
  restrict: string = 'E';
  bindToController: boolean = true;

  /**
   * Template for the current button
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheButtonAttributes) {
    let template: string = this.getTemplateStart();

    template = template + '>';

    if ($attrs.cheButtonIcon) {
      template = template + `<md-icon md-font-icon="${$attrs.cheButtonIcon}" flex layout="column" layout-align="start center"></md-icon>`;
    }

    template = template + $attrs.cheButtonTitle + '</md-button>';
    return template;
  }

  abstract getTemplateStart(): string;

  compile($element: ng.IAugmentedJQuery, $attrs: ICheButtonAttributes): ng.IDirectivePrePost {
    const avoidAttrs = ['ng-model', 'ng-click'];
    const allowedAttrPrefixes = ['ng-'];
    const allowedAttributes = ['href', 'name', 'target', 'tabindex'];

    const mdButtonEl = $element.find('md-button');

    const keys = Object.keys($attrs.$attr);
    keys.forEach((key: string) => {
      const attr = $attrs.$attr[key];
      if (!attr) {
        return;
      }
      if (avoidAttrs.indexOf(attr) !== -1) {
        return;
      }

      const isAllowedPrefix = allowedAttrPrefixes.some((prefix: string) => {
        return attr.indexOf(prefix) === 0;
      });
      const isAllowedAttribute = allowedAttributes.some((_attr: string) => {
        return attr === _attr;
      });
      if (!isAllowedAttribute && !isAllowedPrefix) {
        return;
      }

      let value = $attrs[key];
      if (attr === 'tabindex') {
        value = value || 0;
      }

      mdButtonEl.attr(attr, value);
      $attrs.$set(attr, null);
    });

    return;
  }

}
