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
import {CheAPIBuilder} from './che-api-builder.factory';

/**
 * Tests of the CheWorkspaceBuilder
 * @author Florent Benoit
 */
describe('CheWorkspaceBuilder', () => {


  var wkspBuilder;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));


  /**
   * Inject builder
   */
  beforeEach(inject((cheAPIBuilder: CheAPIBuilder) => {
    wkspBuilder = cheAPIBuilder.getWorkspaceBuilder();
  }));

  /**
   * Check builder
   */
  it('check builder', () => {
    let name = 'hello';
    let workspace = wkspBuilder.withName(name).build();


    // check values
    expect(workspace.config.name).toEqual(name);

  });

  /**
   * Check builder
   */
  it('check builder 1', () => {
    let name = 'hello';
    let id = 'id1';
    let workspace = wkspBuilder.withName('hello').withId('id1').withTemporary(true).build();

    // check values
    expect(workspace.config.name).toEqual(name);
    expect(workspace.id).toEqual(id);
    expect(workspace.temporary).toBeTruthy();
  });

  /**
   * Check builder
   */
  it('check builder 2', () => {
    let workspace: che.IWorkspace = wkspBuilder.withTemporary(false).build();

    // check values
    expect(workspace.temporary).toBeFalsy();
  });

});
