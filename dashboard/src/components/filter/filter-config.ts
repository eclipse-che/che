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
import {CheNumberRoundFilter} from './number-round/number-round.filter';
import {ChangeMemoryUnitFilter} from './change-memory-unit/change-memory-unit.filter';

export class FilterConfig {

  constructor(register: che.IRegisterService) {
    register.filter('numberRound', CheNumberRoundFilter.filter);
    register.filter('changeMemoryUnit', ['$log', ChangeMemoryUnitFilter.filter]);
  }

}
