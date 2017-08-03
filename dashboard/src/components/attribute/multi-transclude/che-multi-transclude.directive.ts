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

  restrict: string = 'AE';
  transclude: boolean = false;

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attr: ng.IAttributes, ctrl: ng.INgModelController, $transclude: ng.ITranscludeFunction): void {

    $transclude(($clone: ng.IAugmentedJQuery) => {
      $element.find('[target]').each((index: number, target: Element) => {
        const targetName = angular.element(target).attr('target');
        for (let i = 0; i < $clone.length; i++) {
          const additionalPartName = $($clone[i]).attr('part');
          if (!additionalPartName || additionalPartName !== targetName) {
            continue;
          }
          angular.element($clone[i]).detach().appendTo(angular.element(target).empty());
        }
      });
    });

  }

}
