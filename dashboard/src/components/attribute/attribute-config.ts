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

import {CheFocusable} from './focusable/che-focusable.directive';
import {CheAutoScroll} from './scroll/che-automatic-scroll.directive';
import {CheListOnScrollBottom} from './scroll/che-list-on-scroll-bottom.directive';
import {CheReloadHref} from './reload-href/che-reload-href.directive';
import {CheFormatOutput} from './format-output/che-format-output.directive';
import {CheOnLongTouch} from './touch/che-on-long-touch.directive';
import {CheOnRightClick} from './click/che-on-right-click.directive';
import {CheTypeNumber} from './input-type/input-number.directive';
import {CheTypeCity} from './input-type/input-city.directive';
import {CheMultiTransclude} from './multi-transclude/che-multi-transclude.directive';
import {CheMultiTranscludePart} from './multi-transclude/che-multi-transclude-part.directive';
import {ImgSrc} from './img-src/img-src.directive';
import {CheClipTheMiddle} from './clip-the-middle/che-clip-the-middle.directive';
import {UiCodemirror} from './codemirror/codemirror.directive';

export class AttributeConfig {

  constructor(register: che.IRegisterService) {

    register.directive('focusable', CheFocusable);

    register.directive('cheAutoScroll', CheAutoScroll);

    register.directive('cheListOnScrollBottom', CheListOnScrollBottom);

    register.directive('cheReloadHref', CheReloadHref);

    register.directive('cheFormatOutput', CheFormatOutput);

    register.directive('cheOnLongTouch', CheOnLongTouch);

    register.directive('cheOnRightClick', CheOnRightClick);

    register.directive('cheTypeNumber', CheTypeNumber);
    register.directive('cheTypeCity', CheTypeCity);

    register.directive('cheMultiTransclude', CheMultiTransclude);
    register.directive('cheMultiTranscludePart', CheMultiTranscludePart);

    register.directive('imgSrc', ImgSrc);

    register.directive('cheClipTheMiddle', CheClipTheMiddle);
    // ui codemirror
    register.directive('uiCodemirror', UiCodemirror);
  }
}
