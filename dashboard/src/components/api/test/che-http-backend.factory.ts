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

import {CheHttpBackend} from './che-http-backend';
import {CheAPIBuilder} from '../builder/che-api-builder.factory';

/**
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 */
export class CheHttpBackendFactory extends CheHttpBackend {

  static $inject = ['$httpBackend', 'cheAPIBuilder'];

  /**
   * Default constructor
   */
  constructor($httpBackend: ng.IHttpBackendService,
              cheAPIBuilder: CheAPIBuilder) {
    super($httpBackend, cheAPIBuilder);
  }

}

