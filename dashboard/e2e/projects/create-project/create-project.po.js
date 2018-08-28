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

function CreateProject() {

  this.containerElement = $('#create-project-content-page');

  // project samples
  this.samplesListElement = $('.projects-create-project-samples-list');
  this.samplesListItemElements = this.samplesListElement.$$('.projects-create-project-samples-list-item');
  this.getSamplesListItemElementByName = (name) => {
    return this.samplesListItemElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('.projects-create-project-samples-list-samplename', name)).isPresent().then(isPresent => isPresent);
    }).first();
  };
  this.getSamplesListItemElementByIndex = (index) => {
    return this.samplesListItemElements.filter((elem, idx) => {
      return index === idx;
    }).first();
  };
  this.isListItemSelected = (elem) => {
    let func = () => {
      let elem = arguments[0],
          className = arguments[1];
      return angular.element(elem).hasClass(className);
    };
    return browser.executeScript(func, elem.getWebElement(), 'projects-create-project-samples-list-item-active');
  };

}

module.exports = new CreateProject();
