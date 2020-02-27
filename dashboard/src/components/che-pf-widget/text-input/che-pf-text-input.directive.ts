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

export interface IChePfTextInputProperties extends IChePfInputProperties {
  config: {
    id?: string;
    name: string;
    pattern?: string;
    placeHolder?: string;
    labelName?: string;
  };
  onChange: ($value: string) => void;
}

interface IChePfTextInputDirectiveBindings extends IChePfInputBindings {
  config: {
    id?: string;
    name: string;
    pattern?: string;
    placeHolder?: string;
    labelName?: string;
  };
  onChange: (eventObj: { $value: string }) => void;
}

interface IChePfTextInputDirectiveScope {
  scope: { [key in keyof IChePfTextInputDirectiveBindings]: string };
}

/**
 * @ngdoc directive
 *
 * @description defines a text input field.
 *
 * @usage
 * <che-pf-text-input
 *   value="$ctrl.textInput.value"
 *   config="$ctrl.textInput.config"
 *   on-change="$ctrl.textInput.onChange($value)">
 * </che-pf-text-input>
 *
 * @author Oleksii Kurinnyi
 */
export class ChePfTextInputDirective extends ChePfInput implements IChePfTextInputDirectiveScope {

  static $inject = [
    '$document',
    'randomSvc',
  ];

  restrict = 'E';
  replace = true;
  transclude = true;

  // we require ngModel as we want to use it inside our directive
  require = '?ngModel';

  templateUrl = 'components/che-pf-widget/text-input/che-pf-text-input.html';

  scope = {
    value: '=?',
    form: '=?',
    config: '=',
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
