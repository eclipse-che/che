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

export interface IChePfMastheadProperties {
  title: string;
}

interface IChePfMastheadBindings extends IChePfMastheadProperties { }

interface IChePfMastheadDirectiveScope {
  scope: { [key in keyof IChePfMastheadBindings]: string };
}

/**
 * @ngdoc directive
 *
 * @description defines a masthead component.
 *
 * @usage
 * <che-pf-masthead
 *   title="$ctrl.masthead.title">
 *
 *   <!-- content to transclude -->
 *
 * </che-pf-masthead>
 *
 * @author Oleksii Kurinnyi
 */
export class ChePfMastheadDirective implements ng.IDirective, IChePfMastheadDirectiveScope {

  scope = {
    title: '@'
  };

  templateUrl = 'components/che-pf-widget/masthead/che-pf-masthead.html';
  transclude = true;
  replace = true;

}
