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

import {CheFocusable} from './focusable/che-focusable.directive';
import {CheAutoScroll} from './scroll/che-automatic-scroll.directive';
import {CheListOnScrollBottom} from './scroll/che-list-on-scroll-bottom.directive';
import {CheReloadHref} from './reload-href/che-reload-href.directive';
import {CheFormatOutput} from './format-output/che-format-output.directive';
import {CheOnLongTouch} from './touch/che-on-long-touch.directive';
import {CheOnRightClick} from './click/che-on-right-click.directive';

export class AttributeConfig {

  constructor(register) {

    register.directive('focusable', CheFocusable);

    register.directive('cheAutoScroll', CheAutoScroll);

    register.directive('cheListOnScrollBottom', CheListOnScrollBottom);

    register.directive('cheReloadHref', CheReloadHref);

    register.directive('cheFormatOutput', CheFormatOutput);

    register.directive('cheOnLongTouch', CheOnLongTouch);

    register.directive('cheOnRightClick', CheOnRightClick);
  }
}
