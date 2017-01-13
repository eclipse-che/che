/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * @ngdoc directive
 * @name demoSourceRender
 *
 * @restrict AE
 *
 * @description
 * `<demo-source-render>` is used to show nested directive source and compiled component.
 *
 * You can use `<textarea demo-source-render></textarea>` to keep original indentation and alignment in nested directive source
 *
 * @usage
 * <demo-source-render>
 *   <other-directive></other-directive>
 * </demo-source-render>
 *
 * <textarea demo-source-render>
 *   <other-directive></other-directive>
 * </textarea>
 *
 * @author Oleksii Kurinnyi
 */

export class DemoSourceRender {
  $compile: ng.ICompileService;

  restrict: string = 'EA';
  replace: boolean = true;
  scope: Object = {};

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($compile: ng.ICompileService) {
    this.$compile = $compile;
  }

  template(element: ng.IAugmentedJQuery) {
    let source, demoElement;
    if (element.prop('tagName') === 'TEXTAREA') {
      source = element.html();
      demoElement = element.text();
    } else {
      source = element.html().replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
      demoElement = element.html();
    }
    // remove last empty line
    source = source.replace(/\n\s*$/, '');

    return `<div class="demo-source-render">
        <pre><code class="demo-source">${source}</code></pre>
        <div class="demo-render">${demoElement}</div>
      </div>`;
  }

}
