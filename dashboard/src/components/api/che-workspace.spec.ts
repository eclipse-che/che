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
import {CheWorkspace} from './che-workspace.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/**
 * Test of the CheWorkspace
 */
describe('CheWorkspace', () => {

    /**
     * Workspace Factory for the test
     */
    var factory;

    /**
     * API builder.
     */
    var apiBuilder;

    /**
     * Backend for handling http operations
     */
    var httpBackend;

    /**
     * Che backend
     */
    var cheBackend;

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
    beforeEach(inject((cheWorkspace: CheWorkspace, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {
        factory = cheWorkspace;
        apiBuilder = cheAPIBuilder;
        cheBackend = cheHttpBackend;
        httpBackend = cheHttpBackend.getHttpBackend();
    }));

    /**
     * Check assertion after the test
     */
    afterEach(() => {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });


    /**
     * Check that we're able to fetch workspaces and calls the listeners
     */
    it('Fetch Workspaces', () => {
        // setup tests objects
        let workspace1 = apiBuilder.getWorkspaceBuilder().withId('123').withName('testWorkspace').build();
        let tmpWorkspace2 = apiBuilder.getWorkspaceBuilder().withId('456').withName('tmpWorkspace').withTemporary(true).build();

        // add the listener
        let listener = new Listener();
        factory.addListener(listener);

        // no workspaces now on factory or on listener
        expect(factory.getWorkspaces().length).toEqual(0);
        expect(listener.getWorkspaces().length).toEqual(0);

        // expecting a GET
        httpBackend.expectGET('/api/workspace');

        // providing request
        // add workspaces on Http backend
        cheBackend.addWorkspaces([workspace1, tmpWorkspace2]);

        // setup backend
        cheBackend.setup();

        // fetch workspaces
        factory.fetchWorkspaces();

        // flush command
        httpBackend.flush();

        // now, check workspaces
        let workspaces = factory.getWorkspaces();

        // check we have only one workspace (temporary workspace is excluded)
        expect(workspaces.length).toEqual(1);

        // check name of the workspaces
        let resultWorkspace1 = workspaces[0];
        expect(resultWorkspace1.config.name).toEqual(workspace1.config.name);

        // check the callback has been called without temporary workspace
        expect(listener.getWorkspaces().length).toEqual(1);
        expect(listener.getWorkspaces()[0].config.name).toEqual(workspace1.config.name);
       }
    );

});
