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

import {
  ICheButtonDropdownMainAction,
  ICheButtonDropdownOtherAction
} from '../../components/widget/button-dropdown/che-button-dropdown.directive';

import {ICheEditModeOverlayConfig} from '../../components/widget/edit-mode-overlay/che-edit-mode-overlay.directive';
import {CheNotification} from '../../components/notification/che-notification.factory';

enum Tab {Font, Panel, Selecter, Icons, Dropdown_button,  Buttons, Input, List, Label_container, Stack_selector, Popover, Edit_mode_overlay}

/**
 * This class is handling the controller for the demo of components
 * @author Florent Benoit
 */
export class DemoComponentsController {

  static $inject = ['$location', 'cheNotification'];

  $location: ng.ILocationService;
  cheNotification: CheNotification;
  selectedIndex: number;
  tab: Object = Tab;

  booksByAuthor: {
    [author: string]: Array<{title: string}>
  };
  button2Disabled: boolean;
  listItemsDocs: string[];
  listItemsTasks: Array<{
    name: string,
    done: boolean
  }>;

  // number spinner
  number: number;
  numberIsChanged: number;

  placement: any;

  buttonDropdownConfig: {
    mainAction: ICheButtonDropdownMainAction;
    otherActions: Array<ICheButtonDropdownOtherAction>;
  };

  overlayConfig: ICheEditModeOverlayConfig;

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService,
              cheNotification: CheNotification) {
    this.$location = $location;
    this.cheNotification = cheNotification;

    const tab = $location.search().tab;
    if (Tab[tab]) {
      this.selectedIndex = parseInt(Tab[tab], 10);
    } else {
      this.selectedIndex = Tab.Font;
    }
    this.placement = {
      options: [
        'top',
        'top-left',
        'top-right',
        'bottom',
        'bottom-left',
        'bottom-right',
        'left',
        'left-top',
        'left-bottom',
        'right',
        'right-top',
        'right-bottom'
      ],
      selected: 'top'
    };
    this.buttonDropdownConfig = {
      mainAction: {
        title: 'Main Action',
        type: 'button'
      },
      otherActions: [{
        title: 'Other action 2',
        type: 'button',
        orderNumber: 2
      }, {
        title: 'Other action 1',
        type: 'button',
        orderNumber: 1
      }]
    };
    this.init();
  }

  init(): void {
    // selecter
    this.booksByAuthor = {};
    this.booksByAuthor['St Exupery'] = [{title: 'The little prince'}];
    this.booksByAuthor['V. Hugo'] = [{title: 'Les miserables'}, {title: 'The Hunchback of Notre-Dame'}];
    this.booksByAuthor['A. Dumas'] = [{title: 'The count of Monte Cristo'}, {title: 'The Three Musketeers'}];


    this.button2Disabled = true;

    this.listItemsDocs = ['Document1', 'Document2', 'Document3', 'Document4', 'Document5'];

    this.listItemsTasks = [{name : 'Task 1', done: false}, {name : 'Task 2', done: true}, {name : 'Task 3', done: false},
      {name : 'Task 4', done: true}, {name : 'Task 5', done: false}];

    // number spinner
    this.number = 0;
    this.numberIsChanged = 0;

    // edit-mode config
    this.overlayConfig = {
      visible: true,
      disabled: false,
      message: {
        content: `Information message`,
        visible: true
      },
      applyButton: {
        action: () => {
          this.cheNotification.showInfo(`Button 'Apply' was clicked.`);
        },
        disabled: false
      },
      saveButton: {
        action: () => {
          this.cheNotification.showInfo(`Button 'Save' was clicked.`);
        },
        disabled: false
      },
      cancelButton: {
        action: () => {
          this.cheNotification.showInfo(`Button 'Cancel' was clicked.`);
        },
        disabled: false
      }
    };
  }

  /**
   * Changes search part of URL.
   *
   * @param {number} tabIndex
   */
  onSelectTab(tabIndex?: number): void {
    let param: { tab?: string } = {};
    if (!angular.isUndefined(tabIndex)) {
      param.tab = Tab[tabIndex];
    }
    if (angular.isUndefined(this.$location.search().tab)) {
      this.$location.replace();
    }
    this.$location.search(param);
  }

  toggleDisabled2() {
    this.button2Disabled = !this.button2Disabled;
  }

  isToggleDisabled2() {
    return this.button2Disabled;
  }

  numberChanged(): void {
    this.numberIsChanged++;
  }

}
