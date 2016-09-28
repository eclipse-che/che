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
 * @ngdoc directive
 * @name navbar.selected.directive:NavBarSelected
 * @description This class is adding a CSS class when element is clicked
 * @author Florent Benoit
 */
export class NavBarSelected {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($rootScope, $location) {
    this.$rootScope = $rootScope;
    this.$location = $location;
    this.restrict = 'A';
    this.replace = false;
    this.controller = 'NavBarSelectedCtrl';
    this.controllerAs = 'navBarSelectedCtrl';
    this.bindToController = true;

  }

  /**
   * Monitor click
   */
  link($scope, element, attrs) {
    let select = (elem) => {
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
    if (attrs['ngHref'] === '#'+this.$location.path()) {
      select(element);
    }

    // highlight item on click
    element.bind('click', (event) => {
      // prevent activating menu item if Ctrl key is pressed
      if (event.ctrlKey) {
        this.$rootScope.selectedNavBarElement.focus();
        return;
      }
      select(element);
    });
    element.bind('mousedown', () => {
      element.addClass('navbar-item-no-hover');
    });
    element.bind('mouseup', () => {
      if (element !== this.$rootScope.selectedNavBarElement) {
        element.blur();
      }
    });
    element.bind('mouseover', () => {
      element.removeClass('navbar-item-no-hover');
    });

    $scope.$on('navbar-selected:set', (event, path) => {
      // unselect previously selected item
      if (this.$rootScope.selectedNavBarElement === element) {
        this.$rootScope.selectedNavBarElement.removeClass('che-navbar-selected');
        delete this.$rootScope.selectedNavBarElement;
      }
      // select item
      if (attrs['ngHref'] === path) {
        select(element);
      }
    });
  }


}

