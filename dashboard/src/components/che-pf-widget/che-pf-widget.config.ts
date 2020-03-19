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

import { ChePfMastheadDirective } from './masthead/che-pf-masthead.directive';
import { ChePfPageMainAreaDirective } from './page-main-area/che-pf-page-main-area.directive';
import { ChePfPageMainSectionDirective } from './page-main-section/che-pf-page-main-section.directive';
import { ChePfSecondaryButtonDirective } from './button/che-pf-secondary-button.directive';
import { ChePfSwitchDirective } from './switch/che-pf-switch.directive';
import { ChePfTextInputDirective } from './text-input/che-pf-text-input.directive';

export class ChePfWidgetConfig {

  constructor(register: che.IRegisterService) {
    register.directive('chePfMasthead', ChePfMastheadDirective);
    register.directive('chePfPageMainArea', ChePfPageMainAreaDirective);
    register.directive('chePfPageMainSection', ChePfPageMainSectionDirective);
    register.directive('chePfSecondaryButton', ChePfSecondaryButtonDirective);
    register.directive('chePfSwitch', ChePfSwitchDirective);
    register.directive('chePfTextInput', ChePfTextInputDirective);
  }

}
