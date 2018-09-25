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

let ListStacks = function() {

  this.listElement = $('.stacks-list-content');

  // header buttons
  this.addStackButtonElement = this.listElement.$('.che-list-add-button');
  this.importButtonElement = this.listElement.$('.che-list-import-button');

  // stacks list
  this.listItemElements = this.listElement.$$('.che-list-item');
  this.splitStackItemByCell = (elem) => {
    return {
      title: elem.$('.stack-item-name'),
      description: elem.$('.stack-item-description'),
      components: elem.$('.stack-item-description'),
      actions: elem.$('.stack-item-actions')
    };
  };
  this.getListItemElementByName = (name) => {
    return this.listItemElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('.stack-item-name', name)).isPresent().then(isPresent => isPresent);
    }).first();
  };
};

module.exports = new ListStacks();
