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

const ChePopup = function() {
  this.popups = $$('che-popup');

  this.getPopupElements = () => {
    return this.popups;
  };

  this.getPopupElementByTitle = (popupTitle) => {
    return element(by.cssContainingText('che-popup', popupTitle));
  };

  this.getButtonElements = (popupElement) => {
    return popupElement.$$('button');
  };

  this.getButtonElementByTitle = (popupElement, buttonTitle) => {
    return popupElement.$('[che-button-title="' + buttonTitle + '"] button');
  };

  this.getCloseIconElement = (popupElement) => {
    return popupElement.$('[ng-click="onClose()"]');
  }
};

module.exports = new ChePopup();
