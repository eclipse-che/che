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
 * This class is handling the service for that element injected to body tag
 * @author Oleksii Orel
 */
export class CheBodyInjectorSvc {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($document, $compile) {
    this.$document = $document;
    this.$compile = $compile;
  }

  /**
   * Add item into the body
   * @param item
   * @returns {boolean} - true if successful
   */
  addElement(item) {
    // The parent of the new element
    let parentElement = this.$document.find('body');
    let itemElement = this.$compile(item)(angular.element(parentElement[0]).scope());
    let itemId = itemElement.attr('id');
    if (!itemId) {
      return false;
    }

    let oldItem = this.$document[0].getElementById(itemId);
    if (oldItem) {
      oldItem.remove();
    }
    parentElement.append(itemElement);
    return true;
  }

  /**
   * Remove item from the body
   * @param item
   * @returns {boolean} - true if successful
   */
  removeElement(item) {
    let itemId = item.attr('id');
    if (!itemId) {
      return false;
    }

    let findElement = this.$document[0].getElementById(itemId);
    if (findElement) {
      findElement.remove();
      return true;
    }
    return false;
  }
}
