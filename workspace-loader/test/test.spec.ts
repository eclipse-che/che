/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

/// <reference path="../src/custom.d.ts" />

'use strict';

import { WorkspaceLoader } from '../src/workspace-loader';
import { Loader } from '../src/loader/loader';
import { che } from '@eclipse-che/api';

// tslint:disable:no-any

describe('Workspace Loader', () => {

    let fakeWorkspaceConfig: che.workspace.Workspace;

    beforeEach(function () {
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
                ide: 'test url'
            },
            config: {
                defaultEnv: 'default',
                'environments': {
                    'default': {
                        machines: {
                            machine: {
                                servers: {
                                    server1: {
                                        attributes: {
                                            type: 'ide'
                                        },
                                        port: '0',
                                        protocol: ''
                                    }
                                }
                            },
                        },
                        recipe: {
                            type: ''
                        }
                    }
                }
            }
        } as che.workspace.Workspace;
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

        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');

        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                resolve(fakeWorkspaceConfig);
            }));

        workspaceLoader.load();

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith('foo/bar');
    });

    describe('if workspace has a preconfigured IDE with query parameters', () => {
        const ideURL = 'ide URL';
        let workspaceLoader: WorkspaceLoader;

        beforeEach(done => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
            spyOn(workspaceLoader, 'getQueryString').and.returnValue('?param=value');

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    fakeWorkspaceConfig.status = 'RUNNING';
                    fakeWorkspaceConfig.runtime = { machines: { ide: { servers: { server1: { attributes: { type: 'ide' }, url: ideURL } } } } } as any;
                    resolve(fakeWorkspaceConfig);
                }));

            spyOn(workspaceLoader, 'openIDE').and.callThrough();
            spyOn(workspaceLoader, 'openURL');
            workspaceLoader.load().then(done);
        });

        it('should call openURL method with correct parameter', () => {
            expect(workspaceLoader.openURL).toHaveBeenCalledWith(ideURL + '?param=value');
        });
    });

    describe('if workspace does not have an IDE server', () => {
        let workspaceLoader: WorkspaceLoader;

        beforeEach(done => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
            spyOn(workspaceLoader, 'getQueryString').and.returnValue('');

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    fakeWorkspaceConfig.status = 'RUNNING';
                    fakeWorkspaceConfig.config.environments['default'].machines = {};
                    fakeWorkspaceConfig.runtime = {} as che.workspace.Runtime;
                    resolve(fakeWorkspaceConfig);
                }));

            spyOn(workspaceLoader, 'openIDE').and.callThrough();
            spyOn(workspaceLoader, 'openURL');
            workspaceLoader.load().then(done);
        });

        it('should open IDE directly', () => {
            expect(workspaceLoader.openURL).toHaveBeenCalledWith(fakeWorkspaceConfig.links.ide);
        });
    });

    describe('if workspace is RUNNING', () => {
        let workspaceLoader: WorkspaceLoader;

        beforeEach(() => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');

            spyOn(workspaceLoader, 'connectMasterApi');

            spyOn(workspaceLoader, 'subscribeWorkspaceEvents');

            spyOn(workspaceLoader, 'openIDE');
        });

        describe('and user owns the workspace or has been granted permissions for shared workspace', () => {

            beforeEach(done => {
                spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                    new Promise(resolve => {
                        fakeWorkspaceConfig.status = 'RUNNING';
                        fakeWorkspaceConfig.runtime = {} as che.workspace.Runtime;
                        resolve(fakeWorkspaceConfig);
                    }));

                workspaceLoader.load().then(done);
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

        describe('and user hasn\'t been granted permissions for shared workspace', () => {

            beforeEach(done => {
                spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                    new Promise(resolve => {
                        fakeWorkspaceConfig.status = 'RUNNING';
                        resolve(fakeWorkspaceConfig);
                    }));

                workspaceLoader.load().then(done);
            });

            it('should not connect to workspace master API', () => {
                expect(workspaceLoader.connectMasterApi).not.toHaveBeenCalled();
            });

            it('should not subscribe to workspace events', () => {
                expect(workspaceLoader.subscribeWorkspaceEvents).not.toHaveBeenCalled();
            });

            it('should not open an IDE', () => {
                expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
            });

        });
    });

    describe('if workspace is STOPPED and then starts successfully', () => {
        let workspaceLoader: WorkspaceLoader;
        let statusChangeCallback: Function;
        let workspaceLoadPromise: Promise<any>;

        beforeEach(done => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    fakeWorkspaceConfig.status = 'STOPPED';
                    resolve(fakeWorkspaceConfig);
                }));

            spyOn(workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();

            spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());

            spyOn(workspaceLoader, 'connectMasterApi').and.callFake(() => {
                done();
                return Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => { },
                    subscribeInstallerOutput: () => { },
                    subscribeWorkspaceStatus: (_workspaceId, callback) => {
                        statusChangeCallback = callback;
                    }
                });
            });

            spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

            workspaceLoadPromise = workspaceLoader.load();
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
            });

            it('should not open an IDE', () => {
                expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
            });

            describe('then becomes RUNNING', () => {

                describe('and user owns workspace or has been granted permissions for shared workspace', () => {

                    beforeEach(done => {
                        (workspaceLoader.getWorkspace as any).and.callFake(() =>
                            new Promise(resolve => {
                                fakeWorkspaceConfig.status = 'RUNNING';
                                fakeWorkspaceConfig.runtime = {} as che.workspace.Runtime;
                                resolve(fakeWorkspaceConfig);
                            }));

                        statusChangeCallback({ status: 'RUNNING' });

                        workspaceLoadPromise.then(done);
                    });

                    it('should open an IDE', () => {
                        expect(workspaceLoader.openIDE).toHaveBeenCalled();
                    });

                });

                describe('and user hasn\'t been granted permissions for shared workspace', () => {

                    beforeEach(done => {
                        (workspaceLoader.getWorkspace as any).and.callFake(() =>
                            new Promise(resolve => {
                                fakeWorkspaceConfig.status = 'RUNNING';
                                resolve(fakeWorkspaceConfig);
                            }));

                        statusChangeCallback({ status: 'RUNNING' });

                        workspaceLoadPromise.then(done);
                    });

                    it('should not open an IDE', () => {
                        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
                    });

                    testLoaderIsHidden();
                    testProgressBarIsHidden();
                    testPromptIsShown();
                });

            });

            describe('then receives an error on websocket', () => {

                beforeEach(done => {
                    statusChangeCallback({ error: 'Something bad happened.' });

                    workspaceLoadPromise.then(done);
                });

                it('should not open an IDE', () => {
                    expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
                });

                testLoaderIsHidden();
                testProgressBarIsHidden();
                testPromptIsShown();

            });

        });

    });

    describe('if workspace is STOPPED and then fails to start', () => {
        let workspaceLoader: WorkspaceLoader;
        let workspaceLoadPromise: Promise<void>;

        beforeEach(done => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    fakeWorkspaceConfig.status = 'STOPPED';
                    resolve(fakeWorkspaceConfig);
                }));

            spyOn(workspaceLoader, 'connectMasterApi').and.callFake(() =>
                Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => { },
                    subscribeInstallerOutput: () => { },
                    subscribeWorkspaceStatus: () => { }
                }));

            spyOn(workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();

            spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => {
                done();
                return Promise.reject();
            });

            spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

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

            beforeEach(done => {
                workspaceLoadPromise.then(done);
            });

            testLoaderIsHidden();
            testProgressBarIsHidden();
            testPromptIsShown();
        });

    });

    describe('show error if workspace became stopped on starting', () => {
        let workspaceLoader: WorkspaceLoader;
        const loader = {
            log: () => undefined,
            hideLoader: () => { },
            showReload: () => { },
            error: () => { },
            onclickConsole: () => { },
            onclickReload: () => true
        };
        let statusChangeCallback: Function;

        beforeEach(done => {
            spyOn(loader, 'error').and.callThrough();
            spyOn(loader, 'log').and.callThrough();
            spyOn(loader, 'hideLoader').and.callThrough();
            spyOn(loader, 'showReload').and.callThrough();

            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    fakeWorkspaceConfig.status = 'STARTING';
                    resolve(fakeWorkspaceConfig);
                }));

            spyOn(workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();

            spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());

            spyOn(workspaceLoader, 'connectMasterApi').and.callFake(() => {
                done();
                return Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => { },
                    subscribeInstallerOutput: () => { },
                    subscribeWorkspaceStatus: (_workspaceId, callback) => {
                        statusChangeCallback = callback;
                    }
                });
            });

            spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

            workspaceLoader.load();
        });

        it('should not open IDE', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

        it('should subscribe to workspace events', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).toHaveBeenCalled();
        });

        it('should not start the workspace', () => {
            expect(workspaceLoader.startWorkspace).not.toHaveBeenCalled();
        });

        it('should not log something', () => {
            expect(loader.log).not.toHaveBeenCalled();
        });

        describe('then receives workspace stopped event on websocket, when workspace starting', () => {
            beforeEach(() => {
                statusChangeCallback({ status: 'STOPPED', prevStatus: 'STARTING', workspaceId: 'someID-bla-bla' });
            });

            it('should not open an IDE', () => {
                expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
            });

            it('should log error', () => {
                expect(loader.error).toBeCalledWith('Workspace stopped.');
            });

            it('should hide loader', () => {
                expect(loader.hideLoader).toHaveBeenCalled();
            });

            it('should show reload', () => {
                expect(loader.showReload).toHaveBeenCalled();
            });
        });
    });

    describe('if workspace is STOPPING', () => {
        let workspaceLoader: WorkspaceLoader;
        let statusChangeCallback: Function;
        let workspaceLoadPromise: Promise<any>;

        beforeEach(done => {
            const loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);

            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');

            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    fakeWorkspaceConfig.status = 'STOPPING';
                    resolve(fakeWorkspaceConfig);
                }));

            spyOn(workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();

            spyOn(workspaceLoader, 'startWorkspace').and.callFake(() =>
                Promise.resolve());

            spyOn(workspaceLoader, 'connectMasterApi').and.callFake(() => {
                done();
                return Promise.resolve({
                    addListener: () => { },
                    subscribeEnvironmentOutput: () => { },
                    subscribeInstallerOutput: () => { },
                    subscribeWorkspaceStatus: (_workspaceId, callback) => {
                        statusChangeCallback = callback;
                    }
                });
            });

            spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

            workspaceLoadPromise = workspaceLoader.load();
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

                beforeEach(done => {
                    (workspaceLoader.getWorkspace as any).and.callFake(() =>
                        new Promise(resolve => {
                            fakeWorkspaceConfig.status = 'RUNNING';
                            fakeWorkspaceConfig.runtime = {} as che.workspace.Runtime;
                            resolve(fakeWorkspaceConfig);
                        }));

                    statusChangeCallback({ status: 'RUNNING' });

                    workspaceLoadPromise.then(done);
                });

                it('should open an IDE', () => {
                    expect(workspaceLoader.openIDE).toHaveBeenCalled();
                });

            });

        });

    });

});

function testLoaderIsHidden() {
    it('should hide loader', () => {
        const workspaceLoaderLabel = document.getElementById('workspace-loader-label');

        expect(workspaceLoaderLabel).toBeTruthy();
        expect(workspaceLoaderLabel.style.display).toEqual('none');
    });
}

function testProgressBarIsHidden() {
    it('should hide loader and progress bar', () => {
        const workspaceLoaderProgress = document.getElementById('workspace-loader-progress');

        expect(workspaceLoaderProgress.style.display).toBeTruthy();
        expect(workspaceLoaderProgress.style.display).toEqual('none');
    });
}

function testPromptIsShown() {
    it('should show message with "try again" prompt', () => {
        const workspaceLoaderReload = document.getElementById('workspace-loader-reload');

        expect(workspaceLoaderReload).toBeTruthy();
        expect(workspaceLoaderReload.style.display).not.toEqual('none');
    });
}
