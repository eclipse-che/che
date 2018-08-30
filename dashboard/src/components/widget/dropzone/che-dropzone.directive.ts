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
import {CheDropZoneCtrl} from './che-dropzone.controller';

export interface ICheDropZoneEventObject extends JQueryEventObject {
  dataTransfer: {
    files: any[];
    getData: (key: string) => string;
    types: string[];
  };
  lengthComputable: boolean;
  loaded: number;
  total: number;
}

/**
 * @ngdoc directive
 * @name components.directive:cheDropzone
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-dropzone>` defines a drop box used to drag and drop data.
 *
 * @param {string=} che-title the title of the panel
 *
 * @usage
 *   <che-dropzone></che-dropzone>
 *
 * @example
 * <example module="userDashboard">
 *   <file name="index.html">
 *     <che-dropzone>This is a drag and drop zone</che-dropzone>
 *   </file>
 * </example>
 * @author Florent Benoit
 */
export class CheDropZone implements ng.IDirective {
  restrict = 'E';
  transclude = true;
  bindToController = true;
  replace = true;

  controller = 'CheDropZoneCtrl';
  controllerAs = 'cheDropZoneCtrl';

  scope = {
    callbackController: '=cheCallbackController'
  };

  /**
   * Template for the current drop zone
   * @returns {string} the template
   */
  template (): string {
    const template = '<div ng-class="cheDropZoneCtrl.dropClass" class="che-dropzone" flex layout="row" layout-align="center center">'
      + '<div>Drag and drop a plug-in</div>'
      + '<div ng-show="cheDropZoneCtrl.errorMessage">{{cheDropZoneCtrl.errorMessage}}</div>'
      + '<md-progress-circular ng-show="cheDropZoneCtrl.waitingDrop" md-theme="maincontent-theme" md-mode="indeterminate">'
      + '</md-progress-circular></div>';
    return template;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attributes: ng.IAttributes, $controller: CheDropZoneCtrl) {
    let innerElement = $element[0];

    innerElement.addEventListener('dragenter', (evt: ICheDropZoneEventObject) =>  {
      $controller.dragEnterCallback(evt);
    });

    innerElement.addEventListener('dragleave', (evt: ICheDropZoneEventObject) =>  {
      $controller.dragLeaveCallback(evt);
    });
    innerElement.addEventListener('dragover', (evt: ICheDropZoneEventObject) =>  {
      $controller.dragoverCallback(evt);
    });

    innerElement.addEventListener('drop', (evt: ICheDropZoneEventObject) =>  {
      $controller.dropCallback(evt);
    });
  }

}
