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

import {FactoryInformationController} from '../information-tab/factory-information/factory-information.controller';
import {FactoryInformation} from '../information-tab/factory-information/factory-information.directive';


export class InformationTabConfig {

  constructor(register: che.IRegisterService) {
    register.controller('FactoryInformationController', FactoryInformationController);
    register.directive('cdvyFactoryInformation', FactoryInformation);
  }
}
