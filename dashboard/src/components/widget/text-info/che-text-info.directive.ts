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
 * Defines a directive for creating text info container.
 * @author Oleksii Orel
 */
export class CheTextInfo {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.replace = true;
    this.transclude = false;
    this.templateUrl = 'components/widget/text-info/che-text-info.html';

    // scope values
    this.scope = {
      textValue: '=cheText',
      hrefValue: '=cheHref',
      labelName: '@cheLabelName',
      copyClipboard: '@cheCopyClipboard'
    };

  }

}
