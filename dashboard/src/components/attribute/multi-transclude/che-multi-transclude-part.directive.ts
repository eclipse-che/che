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
 * @name components.directive:cheMultiTranscludePart
 * @restrict AE
 * @function
 * @element
 *
 * @usage
 *  <my-directive>
 *    <div che-multi-transclude-part="part-one">
 *      <che-button che-button-title="Click" name="clickButton"></che-button>
 *    </div>
 *    <div che-multi-transclude-part="part-two">
 *      <div><span>My long text.</span></div>
 *    </div>
 *  </my-directive>
 *
 * @description
 * `che-multi-transclude-part="part-name"` is used to mark a template section to be transcluded as corresponding part for tag with `che-multi-transclude-target="part-name"`. This directive prevents compiling of inner html.
 *
 * @author Oleksii Kurinnyi
 */
export abstract class CheMultiTranscludePart implements ng.IDirective {
  restrict: string = 'A';
  transclude: boolean = false;

  priority: number = 10;
  terminal: boolean = true;
}
