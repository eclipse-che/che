/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

/// <reference path="../src/custom.d.ts" />

'use strict';

import { WorkspaceLoader } from '../src/index';
import { Loader } from '../src/loader/loader';

describe('Workspace Loader', () => {

    let fakeWorkspaceConfig: che.IWorkspace;

    beforeEach(function() {
        document.body.innerHTML = `<div id="workspace-loader">
            <div id="workspace-loader-label">Loading...</div>
            <div id="workspace-loader-progress">
                <div>
                    <div id="workspace-loader-progress-bar"></div>
                </div>
            </div>
            <div id="workspace-loader-reload">Press F5 or click <a href="#">here</a> to try again.</div>
            </div>
                <div id="workspace-console">
                <div id="workspace-console-container"></div>
            </div>`;

        fakeWorkspaceConfig = {
            status: 'STOPPED',
            links: {
                ide: "test url"
            },
            config: {
                defaultEnv: "default",
                "environments": {
                    "default": {
                        machines: {
                            machine: {
                                servers: {
                                    server1: {
                                        attributes: {
                                            type: "ide"
                                        },
                                        port: 0,
                                        protocol: ""
                                    }
                                }
                            },
                        },
                        recipe: {
                            type: ""
                        }
                    }
                }
            }
        } as che.IWorkspace;
    });

    it('should have "workspace-loader" in DOM', () => {
        const loaderElement = document.getElementById('workspace-loader');
        expect(loaderElement).toBeTruthy();
    });

    it('should not get a workspace if workspace key is not specified', () => {
        const loader = new Loader();
        const workspaceLoader = new WorkspaceLoader(loader);

        spyOn(workspaceLoader, 'getWorkspaceKey');
        spyOn(workspaceLoader, 'getWorkspace');

        workspaceLoader.load();

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).not.toHaveBeenCalled();
    });

    it('should get a workspace by its workspace key', () => {
        const loader = new Loader();
        const workspaceLoader = new WorkspaceLoader(loader);

        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");

        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
            return new Promise((resolve) => {
                resolve(fakeWorkspaceConfig);
            });
        });

        workspaceLoader.load();

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith("foo/bar");
    });

    describe('if workspace has a preconfigured IDE with query parameters', () => {
        let ideURL = "ide URL"
        let workspaceLoader;

        beforeEach((done) => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
            spyOn(workspaceLoader, 'getQueryString').and.returnValue("?param=value");

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'RUNNING';
                    fakeWorkspaceConfig.runtime = { machines: { ide: { servers: { server1: { attributes: { type: "ide" }, url: ideURL } } } } } as any;
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "openIDE").and.callThrough();
            spyOn(workspaceLoader, "openURL");
            workspaceLoader.load().then(done());
        });

        it('should call openURL method with correct parameter', () => {
            expect(workspaceLoader.openURL).toHaveBeenCalledWith(ideURL + "?param=value");
        });
    });

    describe('if workspace does not have an IDE server', () => {
        let workspaceLoader;

        beforeEach((done) => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
            spyOn(workspaceLoader, 'getQueryString').and.returnValue("");

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'RUNNING';
                    fakeWorkspaceConfig.config.environments["default"].machines = {};
                    fakeWorkspaceConfig.runtime = {} as che.IWorkspaceRuntime;
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "openIDE").and.callThrough();
            spyOn(workspaceLoader, "openURL");
            workspaceLoader.load().then(done());
        });

        it('should open IDE directly', () => {
            expect(workspaceLoader.openURL).toHaveBeenCalledWith(fakeWorkspaceConfig.links.ide);
        });
    });

    describe('if workspace is RUNNING', () => {
        let workspaceLoader: WorkspaceLoader;

        beforeEach((done) => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'RUNNING';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "connectMasterApi");

            spyOn(workspaceLoader, "subscribeWorkspaceEvents");

            spyOn(workspaceLoader, "openIDE");

            workspaceLoader.load().then(() => done());
        });

        it('should not connect to workspace master API', () => {
            expect(workspaceLoader.connectMasterApi).not.toHaveBeenCalled();
        });

        it('should not subscribe to workspace events', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).not.toHaveBeenCalled();
        });

        it('should open IDE immediately', () => {
            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });
    });

    describe('if workspace is STOPPED and then starts successfully', () => {
        let workspaceLoader: WorkspaceLoader;
        let statusChangeCallback: Function;

        beforeEach((done) => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'STOPPED';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents").and.callThrough();

            spyOn(workspaceLoader, "startWorkspace").and.callFake(() => {
                return Promise.resolve();
            });

            spyOn(workspaceLoader, "connectMasterApi").and.callFake(() => {
                done();
                return Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => { },
                    subscribeWorkspaceStatus: (workspaceId, callback) => {
                        statusChangeCallback = callback;
                    }
                });
            });

            spyOn(workspaceLoader, "openIDE").and.callFake(() => {
                return Promise.resolve();
            });

            workspaceLoader.load();
        });

        it('should not open an IDE', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

        it('should subscribe to workspace events', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).toHaveBeenCalled();
        });

        it('should start the workspace', () => {
            expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
        });

        describe('then becomes STARTING', () => {

            beforeEach(() => {
                statusChangeCallback({ status: 'STARTING' });
            })

            it('should not open an IDE', () => {
                expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
            });

            describe('then becomes RUNNING', () => {

                beforeEach(() => {
                    statusChangeCallback({ status: 'RUNNING' });
                });

                it('should open an IDE', () => {
                    expect(workspaceLoader.openIDE).toHaveBeenCalled();
                });

            });

        });

    });

    describe('if workspace is STOPPED and then fails to start', () => {
        let workspaceLoader: WorkspaceLoader;
        let startPromiseReject: Function;
        let workspaceLoadPromise: Promise<void>;

        beforeEach((done) => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'STOPPED';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "connectMasterApi").and.callFake(() => {
                return Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => {},
                    subscribeWorkspaceStatus: () => {}
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents").and.callThrough();

            spyOn(workspaceLoader, "startWorkspace").and.callFake(() => {
                done();
                return Promise.reject();
            });

            spyOn(workspaceLoader, "openIDE").and.callFake(() => {
                return Promise.resolve();
            });

            workspaceLoadPromise = workspaceLoader.load();
        });

        it('should not open an IDE immediately', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

        it('should start the workspace', () => {
            expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
        });

        it('should not subscribe to workspace events', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).not.toHaveBeenCalled();
        });

        it('should not open an IDE', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

        describe('then the request for starting the workspace fails', () => {

            beforeEach((done) => {
                workspaceLoadPromise.then(() => done());
            });

            it('should hide loader and progress bar', () => {
                const workspaceLoaderLabel = document.getElementById('workspace-loader-label'),
                    workspaceLoaderProgress = document.getElementById('workspace-loader-progress');

                expect(workspaceLoaderLabel.style.display).toEqual('none');
                expect(workspaceLoaderProgress.style.display).toEqual('none');
            });

            it('should show message with "try again" prompt', () => {
                const workspaceLoaderReload = document.getElementById('workspace-loader-reload');

                expect(workspaceLoaderReload).toBeTruthy();
                expect(workspaceLoaderReload.style.display).not.toEqual('none');
            })

        });

    });

    describe('if workspace is STOPPING', () => {
        let workspaceLoader: WorkspaceLoader;
        let statusChangeCallback: Function;

        beforeEach((done) => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'STOPPING';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents").and.callThrough()

            spyOn(workspaceLoader, "startWorkspace").and.callFake(() => {
                return Promise.resolve();
            });

            spyOn(workspaceLoader, "connectMasterApi").and.callFake(() => {
                done();
                return Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => { },
                    subscribeWorkspaceStatus: (workspaceId, callback) => {
                        statusChangeCallback = callback;
                    }
                });
            });

            spyOn(workspaceLoader, "openIDE").and.callFake(() => {
                return Promise.resolve();
            });

            workspaceLoader.load();
        });

        it('should not open an IDE immediately', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

        it('should set flag to restart a workspace', () => {
            expect(workspaceLoader.startAfterStopping).toEqual(true);
        });

        it('should not start a workspace immediately', () => {
            expect(workspaceLoader.startWorkspace).not.toHaveBeenCalled();
        });

        describe('then becomes STOPPED', () => {

            beforeEach(() => {
                statusChangeCallback({ status: 'STOPPED' });
            });

            it('should start a workspace', () => {
                expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
            });

            it('should not open an IDE', () => {
                expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
            });

            describe('then becomes RUNNING', () => {

                beforeEach(() => {
                    statusChangeCallback({ status: 'RUNNING' });
                });

                it('should open an IDE', () => {
                    expect(workspaceLoader.openIDE).toHaveBeenCalled();
                });

            });

        });

    });

});
