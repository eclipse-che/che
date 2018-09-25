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
import {CheErrorMessagesService} from './che-error-messages.service';

interface IErrorMessagesScope extends ng.IScope {
  messageScope: string;
  messageName: string;
}

/**
 * @ngdoc directive
 * @name components.directive:CheErrorMessages
 * @restrict AE
 * @element
 *
 * @description
 * `<che-error-messages>` is the wrapper for `<ng-message>` elements. The directive handle messages changes and stores list of visible messages using CheErrorMessagesService.
 *
 * @param {string} cheMessageScope
 * @param {string} cheMessageName
 *
 * @usage
 *   <che-input name="name"
 *              required
 *              ng-minlength="3"
 *              ng-maxlength="20">
 *     <che-error-messages che-message-scope="settings"
 *                         che-message-name="Name">
 *       <div ng-message="required">A name is required.</div>
 *       <div ng-message="minlength">The name has to be more than 3 characters long.</div>
 *       <div ng-message="maxlength">The name has to be less than 20 characters long.</div>
 *     </che-error-messages>
 *   </che-input>
 *
 * @author Oleksii Kurinnyi
 */
export class CheErrorMessages {
  static $inject = ['cheErrorMessagesService'];

  restrict: string = 'AE';
  replace: boolean = true;

  scope: {
    [paramName: string]: string;
  };

  cheErrorMessagesService: CheErrorMessagesService;


  /**
   * Default constructor that is using resource injection
   */
  constructor(cheErrorMessagesService: CheErrorMessagesService) {
    this.cheErrorMessagesService = cheErrorMessagesService;

    this.scope = {
      messageScope: '@cheMessageScope',
      messageName: '@cheMessageName'
    };
  }

  link($scope: IErrorMessagesScope, $element: ng.IAugmentedJQuery) {
    $scope.$watch(() => { return $element.find('[ng-message]').length; }, (messagesNumber: number) => {
      if (angular.isDefined(messagesNumber)) {
        this.cheErrorMessagesService.removeMessages($scope.messageScope);
        angular.element($element.find('[ng-message]')).each((index: number, el: Element) => {
          this.cheErrorMessagesService.addMessage($scope.messageScope, $scope.messageName, angular.element(el).text());
        });
      }
    });
  }
}
