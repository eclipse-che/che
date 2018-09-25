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
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheAPI} from '../../../../components/api/che-api.factory';

/* global FileReader */

/**
 * Controller for upload factory from the file.
 * @author Oleksii Orel
 */
export class FactoryFromFileCtrl {

  static $inject = ['$filter', 'cheAPI', 'cheNotification', 'FileUploader'];

  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private uploader: any;
  private isImporting: boolean;
  private factoryContent: any;

  /**
   * Default constructor that is using resource injection
   */
  constructor($filter: ng.IFilterService, cheAPI: CheAPI, cheNotification: CheNotification, FileUploader: any) {

    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;

    // if you want select just one file, you won't need to clear the input
    FileUploader.FileSelect.prototype.isEmptyAfterSelection = function () {
      return true;
    };

    this.uploader = new FileUploader();

    // settings
    this.uploader.queueLimit = 1; // maximum count of files
    this.uploader.autoUpload = true; // automatically upload files after adding them to the queue
    this.uploader.removeAfterUpload = true; // automatically remove files from the queue after uploading

    this.isImporting = this.uploader.isUploading;

    const ctrl = this;

    // filters
    this.uploader.filters.push({
      name: 'sizeFilter',
      fn: (item: any) => {
        // file must not be smaller then some size
        let isValidSize = item.size > 0 && item.size < 500000;

        if (!isValidSize) {
          ctrl.cheNotification.showError('File size error.');
        }
        return isValidSize;
      }
    });

    this.uploader.filters.push({
      name: 'typeFilter',
      fn: (item: any) => {
        // file must be json
        let isValidItem = item.type === 'application/json' || item.type === '';

        if (!isValidItem) {
          ctrl.cheNotification.showError('File type error.');
        }
        return isValidItem;
      }
    });

    // callback
    this.uploader.onAfterAddingFile = function (fileItem: any) {
      let uploadedFileName = fileItem._file.name;
      let reader = new FileReader();

      reader.readAsText(fileItem._file);
      reader.onload = function () {
        try {
          ctrl.factoryContent = $filter('json')(angular.fromJson(reader.result), 2);
          ctrl.cheNotification.showInfo('Successfully loaded file\'s configuration ' + uploadedFileName + '.');
        } catch (e) {
          // invalid JSON
          ctrl.factoryContent = null;
          ctrl.cheNotification.showError('Invalid JSON.');
        }
      };
      reader.onerror = function (error: any) {
        ctrl.cheNotification.showError(error.data.message ? error.data.message : 'Error reading file.');
        console.log('Error reading file');
      };
    };

    this.factoryContent = null;
  }

}
