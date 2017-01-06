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


import {CheHttpBackend} from './che-http-backend';


/**
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 */
export class CheHttpBackendFactory extends CheHttpBackend {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($httpBackend, cheAPIBuilder) {
    super($httpBackend, cheAPIBuilder);
  }

}

