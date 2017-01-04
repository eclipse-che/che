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
 * Defines a directive for checking git URL
 * @author Florent Benoit
 */
export class GitUrlValidator {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='A';
    this.require = 'ngModel';

  }

  /**
   * Check that the GIT URL is compliant
   */
  link(scope, element, attributes, ngModel) {
    ngModel.$validators.gitUrl = function(modelValue) {
      var res = /((git|ssh|http(s)?)|(git@[\w\.]+))(:(\/\/))?([\w\.@\:/\-~]+)(\.git)?(\/)?/.test(modelValue);
      return res;
    };
  }

}
