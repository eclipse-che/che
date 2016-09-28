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

import {CheHttpBackend} from './che-http-backend';

/**
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 */
export class CheHttpBackendProviderFactory {

  /**
   * Build a new Che backend based on the given http backend.
   * @param $httpBackend the backend on which to add calls
   * @returns {CheHttpBackend} the new instance
   */
  buildBackend($httpBackend, cheAPIBuilder) {

    // first, add pass through
    $httpBackend.whenGET(new RegExp('components.*')).passThrough();
    $httpBackend.whenGET(new RegExp('^app.*')).passThrough();


    // return instance
    return new CheHttpBackend($httpBackend, cheAPIBuilder);


  }


}

