/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
