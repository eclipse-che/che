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

interface INavBarSelectedAttributes extends ng.IAttributes {
  href: string;
}

interface INavBarSelectedRootScopeService extends ng.IRootScopeService {
  selectedNavBarElement: ng.IAugmentedJQuery;
}

/**
 * @ngdoc directive
 * @name navbar.selected.directive:NavBarSelected
 * @description This class is adding a CSS class when element is clicked
 * @author Florent Benoit
 */
export class NavBarSelected  implements ng.IDirective {

  static $inject = ['$rootScope', '$location'];

  restrict = 'A';
  replace = false;
  controller = 'NavBarSelectedCtrl';
  controllerAs = 'navBarSelectedCtrl';
  bindToController = true;

  private $rootScope: INavBarSelectedRootScopeService;
  private $location: ng.ILocationService;

  /**
   * Default constructor that is using resource
   */
  constructor ($rootScope: ng.IRootScopeService, $location: ng.ILocationService) {
    this.$rootScope = <INavBarSelectedRootScopeService>$rootScope;
    this.$location = $location;
  }

  /**
   * Monitor click
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: INavBarSelectedAttributes) {
    const select = (elem: ng.IAugmentedJQuery) => {
      // if there is a previous selected element, unselect it
      if (this.$rootScope.selectedNavBarElement) {
        this.$rootScope.selectedNavBarElement.removeClass('che-navbar-selected');
      }
      // select the new element
      this.$rootScope.selectedNavBarElement = elem;
      // add the class
      elem.addClass('che-navbar-selected');
    };

    // highlight item at start
    if ($attrs.href === '#' + this.$location.path()) {
      select($element);
    }

    // highlight item on click
    $element.bind('click', (event: JQueryEventObject) => {
      // prevent activating menu item if Ctrl key is pressed
      if (event.ctrlKey) {
        this.$rootScope.selectedNavBarElement.focus();
        return;
      }
      select($element);
    });
    $element.bind('mousedown', () => {
      $element.addClass('navbar-item-no-hover');
    });
    $element.bind('mouseup', () => {
      if ($element !== this.$rootScope.selectedNavBarElement) {
        $element.blur();
      }
    });
    $element.bind('mouseover', () => {
      $element.removeClass('navbar-item-no-hover');
    });

    $scope.$on('navbar-selected:set', (event: ng.IAngularEvent, path: string) => {
      // unselect previously selected item
      if (this.$rootScope.selectedNavBarElement === $element) {
        this.$rootScope.selectedNavBarElement.removeClass('che-navbar-selected');
        delete this.$rootScope.selectedNavBarElement;
      }
      // select item
      if ($attrs.href === path) {
        select($element);
      }
    });
  }


}

