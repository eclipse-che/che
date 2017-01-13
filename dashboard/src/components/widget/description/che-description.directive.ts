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
 * Defines a directive for creating description widget, which can have "Learn more" link as well.
 *
 * @author Ann Shumilova
 */
export class CheDescription {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.replace = true;
    this.transclude = true;
    this.templateUrl = 'components/widget/description/che-description.html';

    // scope values
    this.scope = {
      linkTitle: '@?cheLinkTitle',
      link: '@?cheLink'
    };
  }

}
