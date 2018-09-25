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

import {ListStacksController} from './list-stacks/list-stacks.controller';
import {StackItemController} from './list-stacks/stack-item/stack-item.controller';
import {StackItem} from './list-stacks/stack-item/stack-item.directive';
import {StackController} from './stack-details/stack.controller';
import {ListComponents} from './stack-details/list-components/list-components.directive';
import {ListComponentsController} from './stack-details/list-components/list-components.controller';
import {EditComponentDialogController} from './stack-details/list-components/edit-component-dialog/edit-component-dialog.controller';
import {SelectTemplateController} from './stack-details/select-template/select-template.controller';
import {SamplesTagFilter} from './stack-details/select-template/samples-tag.filter';
import {BuildStackController} from './list-stacks/build-stack/build-stack.controller';
import {ImportStackService} from './stack-details/import-stack.service';
import {StackValidationService} from './stack-details/stack-validation.service';
import {RecipeEditorController} from './list-stacks/build-stack/recipe-editor/recipe-editor.controller';
import {RecipeEditorDirective} from './list-stacks/build-stack/recipe-editor/recipe-editor.directive';
import {ImportStackController} from './stack-details/import-stack.controller';
import {CheStack} from '../../components/api/che-stack.factory';

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
    register.controller('ImportStackController', ImportStackController);
    register.controller('EditComponentDialogController', EditComponentDialogController);
    register.controller('SelectTemplateController', SelectTemplateController);
    register.filter('samplesTagFilter', SamplesTagFilter.filter);
    register.controller('BuildStackController', BuildStackController);
    register.service('importStackService', ImportStackService);
    register.service('stackValidationService', StackValidationService);

    register.controller('RecipeEditorController', RecipeEditorController);
    register.directive('recipeEditor', RecipeEditorDirective);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: any) => {
      $routeProvider.accessWhen('/stacks', {
        title: 'Stacks',
        templateUrl: 'app/stacks/list-stacks/list-stacks.html',
        controller: 'ListStacksController',
        controllerAs: 'listStacksController'
      })
        .accessWhen('/stack/import', {
          title: () => {
            return 'create';
          },
          templateUrl: 'app/stacks/stack-details/stack.html',
          controller: 'ImportStackController',
          controllerAs: 'stackController',
          resolve: {
            initData: ['cheStack', 'importStackService', (cheStack: CheStack, importStackService: ImportStackService) => {
              return cheStack.fetchStacks().then(() => {
                const stack = importStackService.getStack();
                return {stack};
              });
            }]
          }
        })
        .accessWhen('/stack/:stackId', {
          title: (params: any) => {
            return params.stackId;
          },
          templateUrl: 'app/stacks/stack-details/stack.html',
          controller: 'StackController',
          controllerAs: 'stackController',
          resolve: {
            initData: ['$route', 'cheStack', ($route: ng.route.IRouteService, cheStack: CheStack) => {
              return cheStack.fetchStacks().then(() => {
                const {stackId} = $route.current.params;
                const stack = cheStack.getStackById(stackId);
                return {stackId, stack};
              });
            }]
          }
        });
    }]);
  }
}
