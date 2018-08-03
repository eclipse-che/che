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
import {ICheDropZoneEventObject} from './che-dropzone.directive';

/**
 * @ngdoc controller
 * @name components.controller:CheDropZoneCtrl
 * @description This class is handling the controller of a dropzone
 * @author Florent Benoit
 */
export class CheDropZoneCtrl {

  static $inject = ['$scope', 'lodash'];

  $scope: ng.IScope;
  lodash: any;
  HOVER_KO_CLASS = 'che-dropzone-hover-ko';
  HOVER_OK_CLASS = 'che-dropzone-hover-ok';
  errorMessage: string = null;
  dropClass: string;
  waitingDrop: boolean;
  progressUploadPercent: number;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope, lodash: any) {
    this.$scope = $scope;
    this.lodash = lodash;
  }

  dropCallback(evt: ICheDropZoneEventObject): void {
    evt.stopPropagation();
    evt.preventDefault();

    // handle files
    const files = evt.dataTransfer.files;
    if (files.length > 0) {
      // needs to upload the file
      let formData = new FormData();
      for (let i = 0; i < files.length; i++) {
        formData.append('uploadedFile', files[i]);
      }

      const xhr = new XMLHttpRequest();
      xhr.upload.addEventListener('progress', (evt: ICheDropZoneEventObject) => {
        this.uploadProgress(evt);
      });
      xhr.upload.addEventListener('load', (evt: ICheDropZoneEventObject) => {
        this.uploadLoad(evt, files);
      });
      xhr.upload.addEventListener('error', (evt: ICheDropZoneEventObject) => {
        this.uploadError();
      });
      xhr.upload.addEventListener('abort', (evt: ICheDropZoneEventObject) => {
        this.uploadAbort();
      });
      xhr.open('POST', '/admin/upload');
      xhr.send(formData);
      return;
    }

    const url = evt.dataTransfer.getData('URL');
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
  handleUrl(url: string): void {

    let delegateController = (this.$scope as any).cheDropZoneCtrl.callbackController;

    // promise
    let acceptPromise = delegateController.dropzoneAcceptURL(url);

    // waiting answer
    this.$scope.$apply(() => {
      this.waitingDrop = true;
    });

    acceptPromise.then(() => {
      this.waitingDrop = false;
      this.dropClass = '';
    }, (error: any) => {
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
  uploadProgress(evt: ICheDropZoneEventObject): void {
    this.$scope.$apply(() => {
      if (evt.lengthComputable) {
        this.progressUploadPercent = Math.round(evt.loaded * 100 / evt.total);
      }
    });
  }

  /**
   * Callback when upload to the remote servlet has been done
   */
  uploadLoad(evt: ICheDropZoneEventObject, files: any[]): void {
    // upload is OK then we need to upload every files
    for (let i = 0; i < files.length; i++) {
      this.handleUrl('upload:' + files[i].name);
    }
  }

  /**
   * Callback when we have the error
   */
  uploadError(): void {
    this.$scope.$apply(() => {
      this.waitingDrop = false;
      this.dropClass = this.HOVER_KO_CLASS;
      this.errorMessage = 'Unable to upload files';
    });
  }

  /**
   * Callback when we have aborting of the upload
   */
  uploadAbort(): void {
    this.$scope.$apply(() => {
      this.waitingDrop = false;
      this.dropClass = this.HOVER_KO_CLASS;
      this.errorMessage = 'Unable to upload files';
    });
  }

  dragoverCallback(evt: ICheDropZoneEventObject): void {
    evt.stopPropagation();
    evt.preventDefault();
    const okFiles = evt.dataTransfer && evt.dataTransfer && evt.dataTransfer.types && this.lodash.indexOf(evt.dataTransfer.types, 'Files') >= 0;
    const okURI = evt.dataTransfer && evt.dataTransfer && evt.dataTransfer.types && this.lodash.indexOf(evt.dataTransfer.types, 'text/uri-list') >= 0;

    const ok = okFiles || okURI;

    this.$scope.$apply(() => {
      if (ok) {
        this.dropClass = this.HOVER_OK_CLASS;
      } else {
        this.dropClass = this.HOVER_KO_CLASS;
      }
    });
  }

  dragEnterCallback(evt: ICheDropZoneEventObject): void {
    this.cleanup(evt);
  }

  dragLeaveCallback(evt: ICheDropZoneEventObject): void {
    this.cleanup(evt);
  }

  cleanup(evt: ICheDropZoneEventObject): void {
    evt.stopPropagation();
    evt.preventDefault();
    this.$scope.$apply(() => {
      this.waitingDrop = false;
      this.dropClass = '';
      this.errorMessage = '';
    });
  }

}

