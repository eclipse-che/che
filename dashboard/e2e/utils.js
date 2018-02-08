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

let Utils = function() {

  this.getVisibleInputElement = (containerElement) => {
    return containerElement.$$('input').filter((elem, index) => {
      return elem.isDisplayed().then((isDisplayed) => {
        return isDisplayed;
      });
    }).get(0);
  };

  this.getRadioButtonByLabel = (containerElement, label) => {
    return containerElement.element(by.cssContainingText('md-radio-button', label));
  };

  this.getRandomName = (name) => {
    return name + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
  };

  this.scrollToBottom = (elem) => {
    return browser.executeScript('return angular.element(arguments[0]).scrollTop(angular.element(arguments[0])[0].scrollHeight)', elem);
  }

};

module.exports = new Utils();
