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

import { RandomSvc } from '../../utils/random.service';
import { ChePfInput, IChePfInputProperties, IChePfInputBindings } from '../input/che-pf-input';

export interface IChePfSwitchProperties extends IChePfInputProperties {
  config: {
    id?: string;
    name: string;
    messageOn?: string;
    messageOff?: string;
  };
  onChange: ($value: boolean) => void;
}

interface IChePfSwitchBindings extends IChePfInputBindings {
  config: {
    id?: string;
    name: string;
    messageOn?: string;
    messageOff?: string;
  };
  onChange: (eventObj: { $value: boolean }) => void;
}

interface IChePfSwitchDirectiveScope {
  scope: { [key in keyof IChePfSwitchBindings]: string };
}

/**
 * @ngdoc directive
 *
 * @description defines a switch component.
 * Documentation: https://www.patternfly.org/v4/documentation/core/components/switch#documentation
 *
 * @usage
 * <che-pf-switch
 *   value="$ctrl.switch.value"
 *   config="$ctrl.switch.config"
 *   on-change="$ctrl.switch.onChange($value)">
 * </che-pf-switch>
 *
 * @author Oleksii Kurinnyi
 */
export class ChePfSwitchDirective extends ChePfInput implements IChePfSwitchDirectiveScope {

  static $inject = [
    '$document',
    'randomSvc',
  ];

  restrict = 'E';
  replace = true;
  transclude = true;

  // we require ngModel as we want to use it inside our directive
  require = '?ngModel';

  templateUrl = 'components/che-pf-widget/switch/che-pf-switch.html';

  scope = {
    config: '=',
    value: '=?',
    form: '=?',
    onChange: '&'
  };

  constructor(
    $document: ng.IDocumentService,
    randomSvc: RandomSvc,
  ) {
    super(
      $document,
      randomSvc,
    );
  }

}
