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

export interface IChePfInputProperties {
  value?: any;
  /* not implemented */
  form?: ng.IFormController;
  config: {
    id?: string;
    name: string;
  };
  onChange: (...args: any[]) => void;
}

export interface IChePfInputBindings {
  value: any;
  /* not implemented */
  form: ng.IFormController;
  config: {
    id?: string;
    name: string;
  };
  onChange: (eventObj: {$value: any}) => void;
}

interface IChePfInputScopeService extends IChePfInputBindings, ng.IScope { }

/**
 * Defines the super class for all inputs.
 */
export abstract class ChePfInput implements ng.IDirective {

  private $document: ng.IDocumentService;
  private randomSvc: RandomSvc;

  constructor(
    $document: ng.IDocumentService,
    randomSvc: RandomSvc
  ) {
    this.$document = $document;
    this.randomSvc = randomSvc;
  }

  compile(element: ng.IAugmentedJQuery, attrs: ng.IAttributes): ng.IDirectivePrePost {
    const avoidAttrs = ['ng-model', 'ng-change'];
    const keys = Object.keys(attrs.$attr);
    const inputEl = element.find('input');
    keys.forEach(key => {
      const attr = attrs.$attr[key];
      if (!attr) {
        return;
      }
      if (avoidAttrs.indexOf(attr) !== -1) {
        return;
      }
      const value = attrs[key];
      inputEl.attr(attr, value);
      element.removeAttr(attr);
    });
    return;
  }

  link($scope: IChePfInputScopeService, element: ng.IAugmentedJQuery, attrs: ng.IAttributes): void {
    const config = $scope.config;

    // ensure input ID
    if (!config.id) {
      let idIsUnique = false;
      let id: string;
      while (idIsUnique === false) {
        id = this.randomSvc.getRandString({ prefix: config.name });
        idIsUnique = this.$document.find(`#${id}`).length === 0;
      }
      config.id = id;
    }
  }

}
