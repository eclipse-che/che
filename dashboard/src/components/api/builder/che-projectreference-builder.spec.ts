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
import {CheAPIBuilder} from './che-api-builder.factory';

/**
 * Tests of the CheProjectReferenceBuilder
 * @author Florent Benoit
 */
describe('CheProjectReferenceBuilder', function(){

  /**
   * For creating builders.
   */
  let apiBuilder;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject builder
   */
  beforeEach(inject(function(cheAPIBuilder: CheAPIBuilder) {
    apiBuilder = cheAPIBuilder;
  }));

  /**
   * Check builder
   */
  it('check builder', function() {

    const projectReferenceBuilder = apiBuilder.getProjectReferenceBuilder();

    const name = 'myProject';
    const projectReference = projectReferenceBuilder.withName(name).build();

    // check values
    expect(projectReference.name).toEqual(name);

  });

});
