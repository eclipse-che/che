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
 <example module="userDashboard">
 <file name="index.html">
 <che-dropzone>This is a drag and drop zone</che-dropzone>
 </file>
 </example>
 * @author Florent Benoit
 */
export class CheDropZone {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.transclude = true;
    this.bindToController = true;
    this.replace = true;

    this.controller = 'CheDropZoneCtrl';
    this.controllerAs = 'cheDropZoneCtrl';


    this.scope = {
      callbackController: '=cheCallbackController'
    };

  }


  /**
   * Template for the current drop zone
   * @returns {string} the template
   */
  template(){
    var template = '<div ng-class="cheDropZoneCtrl.dropClass" class="che-dropzone" flex layout="row" layout-align="center center">'
      + '<div>Drag and drop a plug-in</div>'
      + '<div ng-show="cheDropZoneCtrl.errorMessage">{{cheDropZoneCtrl.errorMessage}}</div>'
      + '<md-progress-circular ng-show="cheDropZoneCtrl.waitingDrop" md-theme="maincontent-theme" md-mode="indeterminate">'
      + '</md-progress-circular></div>';
    return template;
  }



  /**
   * Keep reference to the model controller
   */
  link($scope, element, attributes, controller) {
    let innerElement = element[0];

    innerElement.addEventListener('dragenter', (evt) =>  {
      controller.dragEnterCallback(evt);
    });

    innerElement.addEventListener('dragleave', (evt) =>  {
      controller.dragLeaveCallback(evt);
    });
    innerElement.addEventListener('dragover', (evt) =>  {
      controller.dragoverCallback(evt);
    });

    innerElement.addEventListener('drop', (evt) =>  {
      controller.dropCallback(evt);
    });


  }

}
