/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
