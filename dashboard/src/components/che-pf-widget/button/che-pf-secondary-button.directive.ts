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

export interface IChePfSecondaryButtonProperties {
  title: string;
  onClick: () => void;
}

interface IChePfSecondaryButtonBindings extends IChePfSecondaryButtonProperties { }

interface IChePfSecondaryButtonDirectiveScope {
  scope: { [key in keyof IChePfSecondaryButtonBindings]: string };
}

/**
 * @ngdoc directive
 *
 * @description defines a secondary type button.
 * Documentation: https://www.patternfly.org/v4/documentation/core/components/button#documentation
 *
 * @usage
 * <che-pf-secondary-button
 *   title="$ctrl.secondaryButton.title"
 *   on-click="$ctrl.secondaryButton.onClick()">
 * </che-pf-secondary-button>
 *
 * @author Oleksii Kurinnyi
 */
export class ChePfSecondaryButtonDirective implements ng.IDirective, IChePfSecondaryButtonDirectiveScope {

  restrict = 'E';
  replace = true;
  templateUrl = 'components/che-pf-widget/button/che-pf-secondary-button.html';

  scope = {
    title: '@',
    onClick: '&'
  };

}
