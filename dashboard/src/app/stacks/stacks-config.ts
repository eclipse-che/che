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

import {ListStacksController} from './list-stacks/list-stacks.controller';
import {StackItemController} from './list-stacks/stack-item/stack-item.controller';
import {StackItem} from './list-stacks/stack-item/stack-item.directive';
import {StackController} from './stack-details/stack.controller';
import {ListComponents} from './stack-details/list-components/list-components.directive';
import {ListComponentsController} from './stack-details/list-components/list-components.controller';
import {EditComponentDialogController} from './stack-details/list-components/edit-component-dialog/edit-component-dialog.controller';
import {SelectTemplateController} from './stack-details/select-template/select-template.controller';

/**
 * @ngdoc controller
 * @name stacks:StacksConfig
 * @description This class is used for configuring all stacks stuff.
 * @author Ann Shumilova
 */
export class StacksConfig {

  constructor(register: che.IRegisterService) {
    register.controller('ListStacksController', ListStacksController);

    register.controller('StackItemController', StackItemController);
    register.directive('stackItem', StackItem);

    register.controller('ListComponentsController', ListComponentsController);
    register.directive('listComponents', ListComponents);

    register.controller('StackController', StackController);
    register.controller('EditComponentDialogController', EditComponentDialogController);
    register.controller('SelectTemplateController', SelectTemplateController);

    // config routes
    register.app.config(($routeProvider: any) => {
      $routeProvider.accessWhen('/stacks', {
        title: 'Stacks',
        templateUrl: 'app/stacks/list-stacks/list-stacks.html',
        controller: 'ListStacksController',
        controllerAs: 'listStacksController'
      })
        .accessWhen('/stack/:stackId', {
          title: (params: any) => {
            return params.stackId;
          },
          templateUrl: 'app/stacks/stack-details/stack.html',
          controller: 'StackController',
          controllerAs: 'stackController'
        });
    });
  }
}
