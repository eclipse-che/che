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

export interface IChePfPageMainSectionProperties {
  light: boolean;
}

interface IChePfPageMainSectionBindings extends IChePfPageMainSectionProperties { }

interface IChePfPageMainSectionDirectiveScope {
  scope: { [key in keyof IChePfPageMainSectionBindings]: string };
}

/**
 * @ngdoc directive
 *
 * @description defines a section of the page.
 * Documentation: https://www.patternfly.org/v4/documentation/core/components/page#documentation
 *
 * @usage
 * <che-pf-page-main-section
 *   light="$ctrl.pageMainSection.lightBackground">
 *
 *   <!-- content to transclude -->
 *
 * </che-pf-page-main-section>
 *
 * @author Oleksii Kurinnyi
 */

export class ChePfPageMainSectionDirective implements ng.IDirective, IChePfPageMainSectionDirectiveScope {

  scope = {
    light: '='
  };

  transclude = true;
  replace = true;

  template = `<section class="pf-c-page__main-section" ng-class="{'pf-m-light': light}" ng-transclude></section>`;
}
