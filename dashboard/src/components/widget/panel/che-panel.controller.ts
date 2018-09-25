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

/**
 * @ngdoc controller
 * @name components.controller:ChePanelCtrl
 * @description This class is handling the controller of a panel
 * @author Florent Benoit
 */
export class ChePanelCtrl {

  static $inject = ['$scope'];

  $scope: ng.IScope;

  collapse: boolean;
  locked: boolean;
  disabled: boolean;
  id: string;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope) {
    this.collapse = false;

    // in lock mode, we're unable to toggle and see the content
    this.locked = false;

    this.id = '';

    this.$scope = $scope;
  }

  /**
   * Sets the id
   * @param id
   */
  setId(id: string): void {
    this.id = id;

    // listener on events
    this.$scope.$on('chePanel:toggle', (event: ng.IAngularEvent, data: any) => {
      if (data === this.id) {
        this.toggle();
      }
    });

    this.$scope.$on('chePanel:lock', (event: ng.IAngularEvent, data: any) => {
      if (data === this.id) {
        this.lock();
      }
    });

    this.$scope.$on('chePanel:collapse', (event: ng.IAngularEvent, data: any) => {
      if (data === this.id) {
        this.collapse = true;
      }
    });

    this.$scope.$on('chePanel:disabled', (event: ng.IAngularEvent, data: any) => {
      if (data && (data.id === this.id)) {
        this.disabled = data.disabled;
      }
    });
  }

  /**
   * @returns true if the panel is collapsed.
   */
  isCollapsed(): boolean {
    return this.collapse;
  }

  /**
   * Toggle the collapsed mode
   */
  toggle(): void {
    this.collapse = !this.collapse;
  }

  /**
   * @returns {string} the icon to display
   */
  getToggleIcon(): string {
    if (this.locked) {
      return '';
    }

    if (this.isCollapsed()) {
      return 'material-design icon-ic_add_24px';
    } else {
      return 'material-design icon-ic_keyboard_arrow_down_24px';
    }
  }

  lock(): void {
    this.collapse = true;
    this.locked = true;
  }

}
