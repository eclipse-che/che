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

interface IDropdonwMenuAttributes extends ng.IAttributes {
  navbarDropdownRightClick: any;
}

interface IDropdonwMenuRootScope extends ng.IRootScopeService {
  navbarDropdownActiveMenu: HTMLElement;
}

/**
 * @ngDoc directive
 * @name navbar.directive:NavbarDropdownMenu
 * @description This class is handling the directive to show the dropdown menu in the navbar
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdownMenu implements ng.IDirective {

  static $inject = ['$timeout', '$document', '$rootScope'];

  /**
   * Timeout service.
   */
  $timeout: ng.ITimeoutService;
  /**
   * Wrapper for the browser's <code>document</code> object.
   */
  $document: ng.IDocumentService;
  /**
   * Root scope service.
   */
  $rootScope: IDropdonwMenuRootScope;

  restrict: string = 'E';
  bindToController: boolean = true;
  templateUrl: string = 'app/navbar/navbar-dropdown-menu/navbar-dropdown-menu.html';
  controller: string = 'NavbarDropdownMenuController';
  controllerAs: string = 'navbarDropdownMenuController';

  transclude = true;

  // scope values
  scope: {
    [paramName: string]: string;
  } = {
    dropdownItems: '=navbarDropdownItems',
    isDisabled: '=?navbarDropdownDisabled',
    externalCssClass: '@?navbarDropdownExternalClass',
    offset: '@?navbarDropdownOffset'
  };

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService,
              $document: ng.IDocumentService,
              $rootScope: IDropdonwMenuRootScope) {
    this.$timeout = $timeout;
    this.$document = $document;
    this.$rootScope = $rootScope;
  }

  compile($element: ng.IAugmentedJQuery, $attrs: IDropdonwMenuAttributes): ng.IDirectivePrePost {
    let jqButton = $element.find('[ng-transclude]');
    if (angular.isDefined($attrs.navbarDropdownRightClick)) {
      jqButton.attr('ng-click', '');
      jqButton.attr('che-on-right-click', '$mdOpenMenu($event)');
    } else {
      jqButton.attr('ng-click', '$mdOpenMenu($event)');
    }

    return {};
  }

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery): void {
    // store active menu element in rootScope
    let menuContentEl = $element.find('.navbar-dropdown-menu'),
      menuEl = $element.find('md-menu');
    $scope.$watch(() => {
      return menuContentEl.is(':visible');
    }, (visible: boolean) => {
      if (visible) {
        this.$rootScope.navbarDropdownActiveMenu = menuEl[0];
      }
    });

    let self = this;
    this.$document
      .off('.dropdown-menu')
      .on('mousedown.dropdown-menu contextmenu.dropdown-menu', '.md-menu-backdrop', (e: JQueryEventObject) => {
        let eventType = e.type,
          eventWhich = e.which,
          backdropEl = angular.element(e.target);

        if (eventType === 'mousedown') {
          if (eventWhich === 3) {
            // prevent event propagation for right mousedown
            // and wait for contextmenu event
            e.preventDefault();
            e.stopPropagation();
            return false;
          } else {
            eventType = 'click';
          }
        }

        const x = e.clientX,
          y = e.clientY,
          stack = [];
        let elementMouseIsOver = (self.$document[0] as any).elementFromPoint(x, y);
        elementMouseIsOver.style.pointerEvents = 'none';
        stack.push(elementMouseIsOver);

        // iterate elements under cursor
        let limit = 50,
          nextTargetEl;
        while (elementMouseIsOver && elementMouseIsOver.tagName !== 'BODY' && elementMouseIsOver.tagName !== 'MD-MENU' && limit > 0) {
          elementMouseIsOver = (self.$document[0] as any).elementFromPoint(x, y);

          // break when top of tree is reached
          if (stack[stack.length - 1] === elementMouseIsOver) {
            break;
          }

          let curEl = angular.element(elementMouseIsOver);

          // element to trigger event
          if (!nextTargetEl) {
            nextTargetEl = curEl;
          }

          elementMouseIsOver.style.pointerEvents = 'none';
          stack.push(elementMouseIsOver);

          limit--;
        }

        // click on menu's backdrop to hide menu
        backdropEl.triggerHandler('click');

        if (elementMouseIsOver && elementMouseIsOver.tagName === 'MD-MENU') {
          // if menu is found then
          // check if click is caught over the same menu
          if (elementMouseIsOver === this.$rootScope.navbarDropdownActiveMenu) {
            // clear active menu
            delete this.$rootScope.navbarDropdownActiveMenu;
          } else {
            // open new menu by triggering mouse event
            angular.element(nextTargetEl).trigger({
              type: eventType,
              which: eventWhich
            } as JQueryEventObject);
          }
        } else {
          // if menu isn't found
          // just trigger same mouse event on first found element
          angular.element(nextTargetEl).trigger({
            type: eventType,
            which: eventWhich
          } as JQueryEventObject);
        }

        // clean pointer events
        for (let i = 0; i < stack.length; i++) {
          stack[i].style.pointerEvents = '';
        }

        e.preventDefault();
        e.stopPropagation();

        return false;
      });
  }
}
