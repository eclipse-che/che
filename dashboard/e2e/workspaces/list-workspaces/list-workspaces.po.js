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

const ListWorkspaces = function() {
  // popup
  const popupPageObject = require('../../widgets/che-popup/che-popup.po');
  this.popupElement = popupPageObject.getPopupElementByTitle('Remove workspaces');
  this.popupDeleteButtonElement = popupPageObject.getButtonElementByTitle(this.popupElement, 'Delete');
  this.popupCancelButtonElement = popupPageObject.getButtonElementByTitle(this.popupElement, 'Cancel');
  this.popupCloseIconElement = popupPageObject.getCloseIconElement(this.popupElement);

  // notification
  const notificationPageObject = require('../../widgets/che-notification/che-notification.po');
  this.waitForNotificationWithText = (text) => {
    return notificationPageObject.info.waitFotNotificationElementWithText(text);
  };

  this.listElement = $('.workspace-list-content');

  // header
  this.listHeaderElement = this.listElement.$('.che-list-header');

  // header buttons
  this.addWorkspaceButtonElement = this.listHeaderElement.$('.che-list-add-button');
  this.deleteWorkspaceButtonElement = this.listHeaderElement.$('.che-list-delete-button');
  this.searchInputElement = this.listHeaderElement.$('.header-search-input input');
  this.searchClearIconElement = this.listHeaderElement.$('.header-close-icon');

  // header row
  this.headerRowElement = this.listHeaderElement.$('.che-list-item-row');
  this.bulkCheckboxElement = this.headerRowElement.$('.che-checkbox-area md-checkbox');
  this.isBulkCheckboxChecked = () => {
    return this.bulkCheckboxElement.getAttribute('checked').then(isChecked => isChecked);
  };

  // workspaces list
  this.listItemElements = this.listElement.$$('.che-list-item');

  this.getListItemElementByName = (name) => {
    return this.listItemElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('.workspace-item-name', name)).isPresent().then(isPresent => isPresent);
    }).first();
  };

  this.getItemCheckbox = (elem) => {
    return elem.$('.che-checkbox-area md-checkbox');
  };

  this.getListItemCheckedElement = () => {
    return this.listItemElements.filter((elem, index) => {
      const checkboxElement = this.getItemCheckbox(elem);
      return checkboxElement.getAttribute('aria-checked').then((isChecked) => {
        return isChecked === 'true';
      });
    });
  }
};

module.exports = new ListWorkspaces();
