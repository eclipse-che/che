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

import {CheErrorMessages} from './che-error-messages.directive';
import {CheErrorMessagesService} from './che-error-messages.service';

export class CheErrorMessagesConfig {

  constructor(register: che.IRegisterService) {
    register.directive('cheErrorMessages', CheErrorMessages);
    register.service('cheErrorMessagesService', CheErrorMessagesService);

  }
}
