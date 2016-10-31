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

/*global FormData, XMLHttpRequest */

/**
 * @ngdoc controller
 * @name components.controller:CheDropZoneCtrl
 * @description This class is handling the controller of a dropzone
 * @author Florent Benoit
 */
export class CheDropZoneCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, lodash) {
    this.$scope = $scope;
    this.lodash = lodash;
    this.HOVER_KO_CLASS = 'che-dropzone-hover-ko';
    this.HOVER_OK_CLASS = 'che-dropzone-hover-ok';
    this.errorMessage = null;
  }

  dropCallback(evt) {
    evt.stopPropagation();
    evt.preventDefault();

    // handle files
    var files = evt.dataTransfer.files;
    if (files.length > 0) {
      // needs to upload the file
      let formData = new FormData();
      for (var i = 0; i < files.length; i++) {
        formData.append('uploadedFile', files[i]);
      }

      var xhr = new XMLHttpRequest();
      xhr.upload.addEventListener('progress', (evt) => {
        this.uploadProgress(evt, files);
      });
      xhr.upload.addEventListener('load', (evt) => {
        this.uploadLoad(evt, files);
      });
      xhr.upload.addEventListener('error', (evt) => {
        this.uploadError(evt, files);
      });
      xhr.upload.addEventListener('abort', (evt) => {
        this.uploadAbort(evt, files);
      });
      xhr.open('POST', '/admin/upload');
      xhr.send(formData);
      return;
    }

    var url = evt.dataTransfer.getData('URL');
    if (url == null) {
      this.$scope.$apply(() => {
        this.dropClass = this.HOVER_KO_CLASS;
      });
      return;
    }

    this.handleUrl(url);

  }


  /**
   * Handle the url during the drop
   * @param url
   */
  handleUrl(url) {

    let delegateController = this.$scope.cheDropZoneCtrl.callbackController;

    // promise
    let acceptPromise = delegateController.dropzoneAcceptURL(url);

    // waiting answer
    this.$scope.$apply(() => {
      this.waitingDrop = true;
    });

    acceptPromise.then(() => {
      this.waitingDrop = false;
      this.dropClass = '';
    }, (error) => {
      this.waitingDrop = false;
      this.dropClass = this.HOVER_KO_CLASS;
      if (error.data && error.data.message) {
        this.errorMessage = error.data.message;
      } else {
        this.errorMessage = error;
      }
    });

  }


  /**
   * Callback when we have the progress status
   */
  uploadProgress(evt) {
    this.$scope.$apply(() => {
      if (evt.lengthComputable) {
        this.progressUploadPercent = Math.round(evt.loaded * 100 / evt.total);
      }
    });
  }

  /**
   * Callback when upload to the remote servlet has been done
   */
  uploadLoad(evt, files) {
    // upload is OK then we need to upload every files
    for (var i = 0; i < files.length; i++) {
      this.handleUrl('upload:' + files[i].name);
    }
  }

  /**
   * Callback when we have the error
   */
  uploadError() {
    this.$scope.$apply(() => {
      this.waitingDrop = false;
      this.dropClass = this.HOVER_KO_CLASS;
      this.errorMessage = 'Unable to upload files';
    });
  }

  /**
   * Callback when we have aborting of the upload
   */
  uploadAbort() {
    this.$scope.$apply(() => {
      this.waitingDrop = false;
      this.dropClass = this.HOVER_KO_CLASS;
      this.errorMessage = 'Unable to upload files';
    });
  }


  dragoverCallback(evt) {

    evt.stopPropagation();
    evt.preventDefault();
    var okFiles = evt.dataTransfer && evt.dataTransfer && evt.dataTransfer.types && this.lodash.indexOf(evt.dataTransfer.types, 'Files') >= 0;
    var okURI = evt.dataTransfer && evt.dataTransfer && evt.dataTransfer.types && this.lodash.indexOf(evt.dataTransfer.types, 'text/uri-list') >= 0;

    var ok = okFiles || okURI;

    this.$scope.$apply(() => {
      if (ok) {
        this.dropClass = this.HOVER_OK_CLASS;
      } else {
        this.dropClass = this.HOVER_KO_CLASS;
      }
    });
  }


  dragEnterCallback(evt) {
    this.cleanup(evt);
  }

  dragLeaveCallback(evt) {
    this.cleanup(evt);
  }


  cleanup(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    this.$scope.$apply(() => {
      this.waitingDrop = false;
      this.dropClass = '';
      this.errorMessage = '';
    });
  }


}

