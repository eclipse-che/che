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

/**
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdownMenu {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $document, $rootScope) {
    this.$timeout = $timeout;
    this.$document = $document;
    this.$rootScope = $rootScope;


    this.restrict = 'E';
    this.bindToController = true;
    this.templateUrl = 'app/navbar/navbar-dropdown-menu/navbar-dropdown-menu.html';
    this.controller = 'NavbarDropdownMenuCtrl';
    this.controllerAs = 'navbarDropdownMenuCtrl';

    this.transclude = true;

    // scope values
    this.scope = {
      dropdownItems: '=navbarDropdownItems',
      isDisabled: '=?navbarDropdownDisabled',
      externalCssClass: '@?navbarDropdownExternalClass',
      offset: '@?navbarDropdownOffset'
    };
  }

  compile($element, attrs) {
    let jqButton = $element.find('[ng-transclude]');
    if (!angular.isUndefined(attrs['navbarDropdownRightClick'])) {
      jqButton.attr('ng-click', '');
      jqButton.attr('che-on-right-click', '$mdOpenMenu($event)');
    } else {
      jqButton.attr('ng-click', '$mdOpenMenu($event)');
    }
  }

  link($scope, $element, attrs, ctrl) {
    // store active menu element in rootScope
    let menuContentEl = $element.find('.navbar-dropdown-menu'),
      menuEl = $element.find('md-menu'),
      isMenuVisible;
    $scope.$watch(() => {
      return menuContentEl.is(':visible');
    }, (visible) => {
      isMenuVisible = visible;
      if (visible) {
        this.$rootScope.navbarDropdownActiveMenu = menuEl[0];
      }
    });

    this.$document.off().on('mousedown contextmenu', '.md-menu-backdrop', (e) => {
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

      var x = e.clientX,
        y = e.clientY,
        stack = [];
      let elementMouseIsOver = document.elementFromPoint(x, y);
      elementMouseIsOver.style.pointerEvents = 'none';
      stack.push(elementMouseIsOver);

      // iterate elements under cursor
      let limit = 50,
        nextTargetEl;
      while (elementMouseIsOver.tagName !== 'MD-MENU' && limit>0){
        elementMouseIsOver = document.elementFromPoint(x, y);

        // break when top of tree is reached
        if (stack[stack.length-1] === elementMouseIsOver) {
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

      if (elementMouseIsOver.tagName === 'MD-MENU') {
        // if menu is found then
        // check if click is caught over the same menu
        if(elementMouseIsOver === this.$rootScope.navbarDropdownActiveMenu) {
          // clear active menu
          delete this.$rootScope.navbarDropdownActiveMenu;
        } else {
          // open new menu by triggering mouse event
          angular.element(nextTargetEl).trigger({
            type: eventType,
            which: eventWhich
          });
        }
      } else {
        // if menu isn't found
        // just trigger same mouse event on first found element
        angular.element(nextTargetEl).trigger({
          type: eventType,
          which: eventWhich
        });
      }

      // clean pointer events
      for (let i=0; i<stack.length; i++) {
        stack[i].style.pointerEvents = '';
      }

      e.preventDefault();
      e.stopPropagation();

      return false;
    });
  }
}
