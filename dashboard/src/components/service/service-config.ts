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

import {CheConfirmDialogController} from './confirm-dialog/che-confirm-dialog.controller';
import {ConfirmDialogService} from './confirm-dialog/confirm-dialog.service';
import {CheUIElementsInjectorService} from './injector/che-ui-elements-injector.service';
import {ResourcesService} from './resources-service/resources-service';


export class ServiceConfig {

  constructor(register: che.IRegisterService) {
    register.controller('CheConfirmDialogController', CheConfirmDialogController);
    register.service('confirmDialogService', ConfirmDialogService);

    register.service('cheUIElementsInjectorService', CheUIElementsInjectorService);
    register.service('resourcesService', ResourcesService);
  }
}
