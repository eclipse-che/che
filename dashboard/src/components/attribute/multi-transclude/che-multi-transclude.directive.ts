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
 * @ngdoc directive
 * @name components.directive:cheMultiTransclude
 * @restrict AE
 * @function
 * @element
 *
 * @usage
 *  <my-directive>
 *    <div part="part-one">
 *      <button type="button" name="my-button">Click to show</button>
 *    </div>
 *    <div part="part-two">
 *      <div><span>My long text.</span></div>
 *    </div>
 *  </my-directive>
 *
 *  The template of the directive myDirective may look like:
 *  <div class="my-directive" che-multi-transclude>
 *    ...
 *    <div target="part-one"></div>
 *    ...
 *    <div target="part-two"></div>
 *    ...
 *  </div>
 *
 * @description
 * `che-multi-transclude` allows to transclude more than one parts of DOM into a directive.
 *
 * @author Oleksii Kurinnyi
 */
export abstract class CheMultiTransclude implements ng.IDirective {

  static $inject = ['$compile'];

  restrict: string = 'AE';
  transclude: boolean = false;

  /**
   * HTML compiler service.
   */
  private $compile: ng.ICompileService;

  /**
   * Default constructor that is using resource
   */
  constructor($compile: ng.ICompileService) {
    this.$compile = $compile;
  }

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attr: ng.IAttributes, ctrl: ng.INgModelController, $transclude: ng.ITranscludeFunction): void {
    const partAttrName = 'che-multi-transclude-part',
          targetAttrName = 'che-multi-transclude-target';

    $transclude(($clone: ng.IAugmentedJQuery, $childScope: ng.IScope) => {
      for (let i = 0; i < $clone.length; i++) {
        const partName = $($clone[i]).attr(partAttrName);
        if (!partName) {
          continue;
        }
        const targetJq = $element.find(`[${targetAttrName}="${partName}"]`);
        if (targetJq.length === 0) {
          continue;
        }

        const partElementCompiled = this.$compile(angular.element($clone[i].innerHTML))($childScope);
        targetJq.empty().append(partElementCompiled);
      }
    });
  }

}
