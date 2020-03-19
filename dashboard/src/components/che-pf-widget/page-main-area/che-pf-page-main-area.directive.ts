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

/**
 * @ngdoc directive
 *
 * @description defines a main area of the page.
 * Documentation: https://www.patternfly.org/v4/documentation/core/components/page#documentation
 *
 * @usage
 * <che-pf-page-main-area>
 *
 *   <!-- content to transclude -->
 *
 * </che-pf-page-main-area>
 *
 * @author Oleksii Kurinnyi
 */
export class ChePfPageMainAreaDirective implements ng.IDirective {

  templateUrl = 'components/che-pf-widget/page-main-area/che-pf-page-main-area.html';
  transclude = true;
  replace = true;

}
