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
 * This class is handling the service for the injecting UI elements to other widgets from outside.
 * @author Oleksii Orel
 */
export class CheUIElementsInjectorService {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($timeout, $document, $compile) {
    this.$timeout = $timeout;
    this.$document = $document;
    this.$compile = $compile;

    //object of destination elements with id as keys and innerHTML of additional widget as value
    //{ parentElementId: { additionalElementId: additionalElementInnerHTML } }
    this.elementsToInject = {};
  }

  /**
   * Add an element for injection.
   * @param parentElementId the ID of parent element
   * @param additionalElementId the ID of additional element
   * @param additionalElementHTML the innerHTML of additional Element
   * @returns {boolean} - true if successful
   */
  addElementForInjection(parentElementId, additionalElementId, additionalElementHTML) {
    if (!parentElementId || !additionalElementId || !additionalElementHTML) {
      return false;
    }

    if (!this.elementsToInject[parentElementId]) {
      this.elementsToInject[parentElementId] = {};
    }
    this.elementsToInject[parentElementId][additionalElementId] = additionalElementHTML;

    return true;
  }

  /**
   * Inject all additional elements if it possible.
   */
  injectAll() {
    this.$timeout(() => {
      for (let parentElementId in this.elementsToInject) {
        let oneParentElements = this.elementsToInject[parentElementId];
        let parentElement = this.$document[0].getElementById(parentElementId);
        if (!parentElement) {
          continue;
        }

        for (let additionalElementId in oneParentElements) {
          let jqAdditionalElement = angular.element(oneParentElements[additionalElementId]);
          if (!jqAdditionalElement) {
            continue;
          }
          // set an id into the additional element
          jqAdditionalElement.attr('id', additionalElementId);
          this.injectAdditionalElement(parentElement, jqAdditionalElement);
        }
      }
    });
  }

  /**
   *  Inject an additional elements if it possible.
   * @param parentElement the parent DOM element for injection
   * @param jqAdditionalElement the jqLite additional element
   * @returns {boolean} - true if successful
   */
  injectAdditionalElement(parentElement, jqAdditionalElement) {
    if (!jqAdditionalElement || !parentElement) {
      return false;
    }

    let additionalElementId = jqAdditionalElement.attr('id');
    if (!additionalElementId) {
      return false;
    }

    let jqParentElement = angular.element(parentElement);

    // compile the view
    let compileAdditionalElement = this.$compile(jqAdditionalElement)(jqParentElement.scope());

    this.deleteElementById(additionalElementId);

    jqParentElement.append(compileAdditionalElement);

    return true;
  }

  /**
   *  Inject an additional element if it possible.
   * @param parentElementId the ID of parent element for injection
   * @param additionalElement the additional element
   * @returns {boolean} - true if successful
   */
  injectAdditionalElementByParentId(parentElementId, additionalElement) {
    if (!additionalElement) {
      return false;
    }

    let jqAdditionalElement = angular.element(additionalElement);

    let additionalElementId = jqAdditionalElement.attr('id');
    if (!additionalElementId) {
      return false;
    }

    let parentElement = this.$document[0].getElementById(parentElementId);
    if (!parentElement) {
      return false;
    }

    return this.injectAdditionalElement(parentElement, jqAdditionalElement);
  }

  /**
   * delete a DOM element by id.
   * @param elementId the ID of element
   * @returns {boolean} - true if successful
   */
  deleteElementById(elementId) {
    return this.$document.find('#' + elementId).remove().length > 0;
  }
}
