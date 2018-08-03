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

interface IPopoverScope extends ng.IScope {
  stateOnChange: (isOpen: boolean) => void;
  isPopoverOpen: boolean;

  title?: string;
  placement?: string;
  isOpen?: boolean;
  onChange: (data: {isOpen: boolean}) => void;
  triggerOutsideClick?: boolean;
}

/**
 * @ngdoc directive
 * @name components.directive:CheTogglePopover
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-toggle-popover>` defines two state popover.
 * This directive uses che-multi-transclude to provide ability to define the popover's button and content at once.
 * The button should be wrapped with
 *   <div part="button"> ... </div>
 *
 * and popover's content should be wrapped with
 *   <div part="popover"> ... </div>
 *
 * @param {string=} title popover's title
 * @param {string=} placement popover's placement
 * @param {expression=} isOpen model
 * @param {Function} on-change callback on model change
 * @param {expression=} trigger-outside-click if <code>true</close> then click outside of popover will close the it
 * @usage
 *   <che-toggle-popover title="Filter"
 *                       on-change="ctrl.filterStateOnChange(isOpen)">
 *     <div part="button">
 *       <button>My button</button>
 *     </div>
 *     <div part="popover">
 *       <div>My popover</div>
 *     </div>
 *   </che-toggle-popover>
 *
 * @author Oleksii Kurinnyi
 */
export class CheTogglePopover implements ng.IDirective {
  static $inject = ['$compile', '$timeout'];

  restrict = 'E';
  transclude = true;
  scope = {
    title: '@?',
    placement: '@?',
    isOpen: '=?isOpen',
    onChange: '&?',
    triggerOutsideClick: '=?'
  };

  private $compile: ng.ICompileService;
  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($compile: ng.ICompileService, $timeout: ng.ITimeoutService) {
    this.$compile = $compile;
    this.$timeout = $timeout;
  }

  /**
   * Template for the toggle-button-popover
   * @returns {string} the template
   */
  template(): string {
    return `
      <div class="che-toggle-popover" che-multi-transclude>
        <div popover-title="{{title ? title : ''}}"
             popover-placement="{{placement ? placement : 'bottom'}}"
             popover-is-open="isPopoverOpen"
             popover-trigger="{{triggerOutsideClick ? 'outsideClick' : 'none'}}"
             uib-popover-html="'<div class=\\'che-transclude\\'></div>'">
          <div che-multi-transclude-target="button"
                is-open="{{isOpen}}"
               ng-click="isOpen=!isOpen;"
               class="test-class"
               ng-class="{'che-toggle-popover-button-disabled': isOpen===false}"></div>
        </div>
      </div>
    `;
  }

  link($scope: IPopoverScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes, ctrl: any, $transclude: ng.ITranscludeFunction): void {

    const watchers: Array<Function> = [];
    watchers.push(
      $scope.$watch(() => { return $scope.isOpen; }, (newVal: boolean, oldVal: boolean) => {
        if (newVal === oldVal) {
          return;
        }

        $scope.stateOnChange($scope.isOpen);
      })
    );

    if ($scope.triggerOutsideClick) {
      // update toggle single button state after popover is closed by outside click
      watchers.push(
        $scope.$watch(() => { return $scope.isPopoverOpen; }, (newVal: boolean, oldVal: boolean) => {
          if (newVal === oldVal) {
            return;
          }
          $scope.isOpen = newVal;
        })
    );
    }
    $scope.$on('$destroy', () => {
      watchers.forEach((watcher: Function) => {
        watcher();
      });
    });

    let childScope: ng.IScope;
    $scope.stateOnChange = (isOpen: boolean) => {
      this.$timeout(() => {
        $scope.isPopoverOpen = isOpen;
      });
      this.$timeout(() => {
        if (angular.isFunction($scope.onChange)) {
          $scope.onChange({isOpen: isOpen});
        }
        if (isOpen) {
          $transclude(($clonedElement: ng.IAugmentedJQuery, $clonedScope: ng.IScope) => {
            childScope = $clonedScope;
            const popover = angular.element('<div></div>').append($clonedElement).find('[che-multi-transclude-part="popover"]'),
                  popoverCompiled = this.$compile(angular.element(popover.html()))($clonedScope);
            $element.find('.che-transclude').replaceWith(popoverCompiled);
          });
        } else {
          if (childScope) {
            childScope.$destroy();
          }
        }
      });
    };
    $scope.stateOnChange($scope.isOpen);

    // close popover on Esc is pressed
    $element.attr('tabindex', 0);
    $element.on('keypress keydown', (event: any) => {
      if (event.which === 27) {
        // on press 'esc'
        $scope.$apply(() => {
          $scope.isOpen = false;
        });
      }
    });

  }

}
