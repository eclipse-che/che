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
 * Defines a directive for a scrolled to bottom list event.
 * @author Michail Kuznyetsov
 */
export class CheListOnScrollBottom {
  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='A';
  }

  /**
   * Check if scroll has reached the bottom
   */
  link(scope, element, attrs) {
    var raw = element[0];
    element.bind('scroll', function () {
      if (raw.scrollTop + raw.offsetHeight - raw.scrollHeight >= 0) {
        scope.$apply(attrs.cheListOnScrollBottom);
      }
    });
  }
}
