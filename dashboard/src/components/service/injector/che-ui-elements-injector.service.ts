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


/**
 * This class is handling the service for the injecting UI elements to other widgets from outside.
 * @author Oleksii Orel
 */
export class CheUIElementsInjectorService {

  static $inject = ['$timeout', '$document', '$compile'];

  $compile: ng.ICompileService;
  $timeout: ng.ITimeoutService;
  $document: ng.IDocumentService;
  elementsToInject: Object;

  /**
   * Default constructor that is using resource injection
   */
  constructor($timeout: ng.ITimeoutService, $document: ng.IDocumentService, $compile: ng.ICompileService) {
    this.$timeout = $timeout;
    this.$document = $document;
    this.$compile = $compile;

    // object of destination elements with id as keys and innerHTML of additional widget as value
    // { parentElementId: { additionalElementId: additionalElementInnerHTML } }
    this.elementsToInject = {};
  }

  /**
   * Add an element for injection.
   * @param parentElementId {string} - the ID of parent element
   * @param additionalElementId {string}  - the ID of additional element
   * @param additionalElementHTML {string}  - the innerHTML of additional Element
   * @returns {boolean} - true if successful
   */
  addElementForInjection(parentElementId: string, additionalElementId: string, additionalElementHTML: string): boolean {
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
  injectAll(): void {
    this.$timeout(() => {
      angular.forEach(this.elementsToInject, (parentElementId: string) => {
        let oneParentElements = this.elementsToInject[parentElementId];
        let parentElement = this.$document.find('#' + parentElementId);
        if (!parentElement) {
          return;
        }

        angular.forEach(oneParentElements, (additionalElementId: string) => {
          let jqAdditionalElement = angular.element(oneParentElements[additionalElementId]);
          if (!jqAdditionalElement) {
            return;
          }
          // set an id into the additional element
          jqAdditionalElement.attr('id', additionalElementId);
          this.injectAdditionalElement(parentElement, jqAdditionalElement);
        });
      });
    });
  }

  /**
   *  Inject an additional elements if it possible.
   * @param parentElement {string | ng.IRootElementService} - the parent DOM element for injection
   * @param additionalElement {string | ng.IRootElementService} - the jqLite additional element
   * @param scope? {ng.IScope}
   * @returns {boolean} - true if successful
   */
  injectAdditionalElement(parentElement: string | ng.IRootElementService, additionalElement: string | ng.IRootElementService, scope?: ng.IScope): boolean {
    if (!additionalElement || !parentElement) {
      return false;
    }

    let jqAdditionalElement = angular.element(additionalElement);
    let additionalElementId = jqAdditionalElement.attr('id');
    if (!additionalElementId) {
      return false;
    }

    let jqParentElement = angular.element(parentElement);
    let additionalElementScope: ng.IScope;
    if (scope) {
      additionalElementScope = scope;
    } else {
      additionalElementScope = jqParentElement.scope();
    }

    // compile the view
    let compileAdditionalElement = this.$compile(jqAdditionalElement)(additionalElementScope);

    this.deleteElementById(additionalElementId);

    jqParentElement.append(compileAdditionalElement);

    return true;
  }

  /**
   *  Inject an additional element if it possible.
   * @param parentElementId {string} - the ID of parent element for injection
   * @param additionalElement {string | ng.IAugmentedJQuery} - the additional element
   * @returns {boolean} - true if successful
   */
  injectAdditionalElementByParentId(parentElementId: string, additionalElement: string | ng.IRootElementService): boolean {
    if (!additionalElement) {
      return false;
    }

    let jqAdditionalElement = angular.element(additionalElement);

    let additionalElementId = jqAdditionalElement.attr('id');
    if (!additionalElementId) {
      return false;
    }

    let parentElement = this.$document.find('#' + parentElementId);
    if (!parentElement) {
      return false;
    }

    return this.injectAdditionalElement(parentElement, jqAdditionalElement);
  }

  /**
   * delete a DOM element by id.
   * @param elementId {string} - the ID of element
   * @returns {boolean} - true if successful
   */
  deleteElementById(elementId: string): boolean {
    return this.$document.find('#' + elementId).remove().length > 0;
  }
}
