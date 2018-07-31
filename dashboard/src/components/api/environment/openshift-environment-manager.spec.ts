/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {OpenshiftEnvironmentManager} from './openshift-environment-manager';
import {CheRecipeTypes} from '../recipe/che-recipe-types';

/**
 * Test the environment manager for openshift based recipes
 * @author Oleksii Orel
 */

describe('OpenshiftEnvironmentManager', () => {
  let envManager: OpenshiftEnvironmentManager;

  beforeEach(inject(($log: ng.ILogService) => {
    envManager = new OpenshiftEnvironmentManager($log);
  }));

  it(`should return 'openshift' recipe type`, () => {
    expect(envManager.type).toEqual(CheRecipeTypes.OPENSHIFT);
  });
});

