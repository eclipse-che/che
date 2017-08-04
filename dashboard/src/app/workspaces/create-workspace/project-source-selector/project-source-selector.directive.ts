/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

export interface IProjectSourceSelectorScope extends ng.IScope {
  updateWidget(activeButtonId: string): void;
}

/**
 * Defines a directive for the project selector.
 *
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class ProjectSourceSelector implements ng.IDirective {
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
   * @ngInject for Dependency injection
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

link($scope: IProjectSourceSelectorScope, $element: ng.IAugmentedJQuery): void {
    $scope.updateWidget = (activeButtonId: string) => {
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
        let top = selectButton.position().top + (selectButton.height() / 2);

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
      });
    };
  }

}
