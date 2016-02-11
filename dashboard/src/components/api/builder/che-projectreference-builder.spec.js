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

/**
 * Tests of the CheProjectReferenceBuilder
 * @author Florent Benoit
 */
describe('CheProjectReferenceBuilder', function(){

  /**
   * For creating builders.
   */
  var apiBuilder;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));


  /**
   * Inject builder
   */
  beforeEach(inject(function(cheAPIBuilder) {
    apiBuilder = cheAPIBuilder;
  }));

  /**
   * Check builder
   */
  it('check builder', function() {

    var projectReferenceBuilder = apiBuilder.getProjectReferenceBuilder();

    var name = 'myProject';
    var projectReference = projectReferenceBuilder.withName(name).build();


    // check values
    expect(projectReference.name).toEqual(name);

  });



});
