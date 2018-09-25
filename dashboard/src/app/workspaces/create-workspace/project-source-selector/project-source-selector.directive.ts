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

export interface IProjectSourceSelectorScope extends ng.IScope {
  updateWidget: (activeButtonId: string, scrollWidgetInView: boolean) => void;
}

/**
 * Defines a directive for the project selector.
 *
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class ProjectSourceSelector implements ng.IDirective {

  static $inject = ['$timeout'];

  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/project-source-selector/project-source-selector.html';
  replace: boolean = true;

  controller: string = 'ProjectSourceSelectorController';
  controllerAs: string = 'projectSourceSelectorController';

  bindToController: boolean = true;

  scope = {};

  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  link($scope: IProjectSourceSelectorScope, $element: ng.IAugmentedJQuery): void {
    $scope.updateWidget = (activeButtonId: string, scrollToBottom: boolean) => {
      this.$timeout(() => {
        const popover = $element.find('.project-source-selector-popover'),
              arrow = popover.find('.arrow'),
              selectButton = $element.find(`#${activeButtonId} button`);
        if (!selectButton || !selectButton.length) {
          popover.removeAttr('style');
          arrow.removeAttr('style');
          return;
        }
        const widgetHeight = $element.height();
        const top = selectButton.position().top + (selectButton.height() / 2);

        const popoverHeight = popover.height();
        if (popoverHeight < top) {
          if ((top + popoverHeight / 2) < widgetHeight) {
            popover.attr('style', `top: ${top - (popoverHeight / 2 + 8)}px;`);
            arrow.attr('style', 'top: 50%;');
          } else {
            popover.attr('style', `top: ${top - popoverHeight}px;`);
            arrow.attr('style', `top: ${popoverHeight}px;`);
          }
        } else {
          popover.attr('style', 'top: 0px;');
          arrow.attr('style', `top: ${top}px;`);
        }

        if (scrollToBottom === false) {
          return;
        }

        // scroll to bottom of the page
        // to make 'Create' button visible
        const mdContent = $element.closest('md-content'),
              mdContentHeight = mdContent.height();
        mdContent.scrollTop(mdContentHeight);
      });
    };
  }

}
