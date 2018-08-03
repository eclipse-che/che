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

interface ICheLinkAttributes extends ng.IAttributes {
  cheLinkText: string;
  ngHref: string;
  target: string;
}

/**
 * Defines the link component.
 * Uses the following options:
 * * che-link-text for the visible content text of the link
 * * ng-href fot the destination url
 * * target fot the HTML link target value (_self, _blank, _parent, _top) - see {@link https://developer.mozilla.org/fr/docs/Web/HTML/Element/a#attr-target}
 * * che-no-padding if truthy, do not add the horizontal and vertical padding and margin
 */
export class CheLink implements ng.IDirective {

  restrict = 'E';
  bindToController = true;

  /**
   * Template for the link component.
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheLinkAttributes): string {
    let linkText = $attrs.cheLinkText || '';
    let destination = $attrs.ngHref ? `ng-href="${$attrs.ngHref}"` : '';
    let noPadding = $attrs.hasOwnProperty('cheNoPadding') ? 'che-link-no-padding' : '';
    let target = '';
    if ($attrs.target) {
      target = `target="${$attrs.target}"`;
    } else if ($attrs.hasOwnProperty('cheSameWindow')) {
      target = 'target="_top"';
    } else if ($attrs.hasOwnProperty('cheNewWindow')) {
      target = 'target="_blank"';
    } else if ($attrs.hasOwnProperty('cheSameFrame')) {
      target = 'target="_self"';
    }

    const template = `<a che-link md-theme="default" class="che-link ${noPadding} md-primary md-no-ink md-hue-2" ${destination} ${target}>${linkText}</a>`;

    return template;
  }


}
