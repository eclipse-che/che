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
import {CheWorkspace} from './che-workspace.factory';
import {CheAPIBuilder} from '../builder/che-api-builder.factory';
import {CheWorkspaceClientBackend} from '../test/che-workspace-client-backend';
import {IBackend} from './che-workspace-rest-client.service';
import {CheHttpBackend} from '../test/che-http-backend';

/**
 * Test of the CheWorkspace
 */
describe('CheWorkspace', () => {

  /**
   * Workspace Factory for the test
   */
  let cheWorkspace: CheWorkspace;

  /**
   * API builder.
   */
  let cheApiBuilder: CheAPIBuilder;

  /**
   * Backend for handling http operations
   */
  let moxiosBackend: IBackend;
  let cheHttpBackend: CheHttpBackend;

  /**
   * Che workspace client backend
   */
  let cheWorkspaceClientBackend: CheWorkspaceClientBackend;

  /**
   * Listener used for the tests
   */
  function Listener() {
    this.workspaces = [];
    this.onChangeWorkspaces = (remoteWorkspaces: Array<any>) => {
      this.workspaces = remoteWorkspaces;
    };
    this.getWorkspaces = () => {
      return this.workspaces;
    };
  }

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((_cheWorkspace_: CheWorkspace,
                     _cheAPIBuilder_: CheAPIBuilder,
                     _cheHttpBackend_: CheHttpBackend,
                     _cheWorkspaceClientBackend_: CheWorkspaceClientBackend) => {
    cheWorkspace = _cheWorkspace_;
    cheApiBuilder = _cheAPIBuilder_;
    cheHttpBackend = _cheHttpBackend_;
    cheWorkspaceClientBackend = _cheWorkspaceClientBackend_;
    moxiosBackend = cheWorkspaceClientBackend.getBackend();

    moxiosBackend.install();
  }));

  afterEach(() => {
    moxiosBackend.uninstall();
  });

  /**
   * Check that we're able to fetch workspaces and calls the listeners
   */
  it('Fetch Workspaces', (done: () => {}) => {
      // setup tests objects
      let workspace1 = cheApiBuilder.getWorkspaceBuilder().withId('123').withName('testWorkspace').build();
      let tmpWorkspace2 = cheApiBuilder.getWorkspaceBuilder().withId('456').withName('tmpWorkspace').withTemporary(true).build();

      // add the listener
      let listener = new Listener();
      cheWorkspace.addListener(listener);

      // no workspaces now on factory or on listener
      expect(cheWorkspace.getWorkspaces().length).toEqual(0);
      expect(listener.getWorkspaces().length).toEqual(0);

      // providing request
      // add workspaces on Http backend
      cheWorkspaceClientBackend.addWorkspaces([workspace1, tmpWorkspace2]);

      // setup backend
      cheWorkspaceClientBackend.setup();
      cheHttpBackend.setup();

      // fetch workspaces
      cheWorkspace.fetchWorkspaces();

      moxiosBackend.wait(() => {
        // now, check workspaces
        let workspaces = cheWorkspace.getWorkspaces();

        // check we have only one workspace (temporary workspace is excluded)
        expect(workspaces.length).toEqual(1);

        // check name of the workspaces
        let resultWorkspace1 = workspaces[0];
        expect(resultWorkspace1.config.name).toEqual(workspace1.config.name);

        // check the callback has been called without temporary workspace
        expect(listener.getWorkspaces().length).toEqual(1);
        expect(listener.getWorkspaces()[0].config.name).toEqual(workspace1.config.name);

        done();
      });
    }
  );

});
