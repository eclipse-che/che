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

import {WorkspaceLoader} from '../src/index';
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

    it('must have "workspace-loader" in DOM', () => {
        const loader = document.getElementById('workspace-loader');
        expect(loader).toBeTruthy();
    });

    it('test when workspace key is not specified', () => {
        let loader = new Loader();
        let workspaceLoader = new WorkspaceLoader(loader);

        spyOn(workspaceLoader, 'getWorkspaceKey');
        spyOn(workspaceLoader, 'getWorkspace');
        
        workspaceLoader.load();

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).not.toHaveBeenCalled();
    });

    it('test getWorkspace with test value', () => {
        let loader = new Loader();
        let workspaceLoader = new WorkspaceLoader(loader);

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

    describe('must open IDE directly when workspace does not have IDE server', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
            spyOn(workspaceLoader, 'getQueryString').and.returnValue("");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.config.environments["default"].machines = {};
                    resolve(fakeWorkspaceConfig);
                });
            });
    
            spyOn(workspaceLoader, "handleWorkspace");

            spyOn(workspaceLoader, "openURL").and.callFake(() => {
                done();
            });

            workspaceLoader.load();
        });

        it('basic workspace function must be called', () => {
            expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
            expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith("foo/bar");
        });

        it('handleWorkspace must not be called', () => {
            expect(workspaceLoader.handleWorkspace).not.toHaveBeenCalled();
        });

        it('must open IDE with `test url`', () => {
            expect(workspaceLoader.openURL).toHaveBeenCalledWith("test url");
        });
    });

    describe('must open default IDE with query parameters when workspace does not have IDE server', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
            spyOn(workspaceLoader, 'getQueryString').and.returnValue("?param=value");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.config.environments["default"].machines = {};
                    resolve(fakeWorkspaceConfig);
                });
            });
    
            spyOn(workspaceLoader, "handleWorkspace");

            spyOn(workspaceLoader, "openURL").and.callFake(() => {
                done();
            });

            workspaceLoader.load();
        });

        it('must open IDE with `test url` and query param `param=value`', () => {
            expect(workspaceLoader.openURL).toHaveBeenCalledWith("test url?param=value");
        });
    });

    describe('must handle workspace when it has IDE server', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    resolve(fakeWorkspaceConfig);
                });
            });
    
            spyOn(workspaceLoader, "handleWorkspace").and.callFake(() => {
                done();
            });

            workspaceLoader.load();
        });

        it('basic workspace function must be called', () => {
            expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
            expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith("foo/bar");
        });

        it('must be called', () => {
            expect(workspaceLoader.handleWorkspace).toHaveBeenCalled();
        });
    });

    describe('must open IDE for RUNNING workspace', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'RUNNING';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents");
    
            spyOn(workspaceLoader, "openIDE").and.callFake(() => {
                done();
            });

            workspaceLoader.load();
        });

        it('must not subscribe to events', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).not.toHaveBeenCalled();
        });
        
        it('must open IDE immediately', () => {
            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });
    });
    
    describe('> must start STOPPED workspace', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'STOPPED';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents").and.callFake(() => {
                return new Promise((resolve) => {
                    resolve();
                });
            });
    
            spyOn(workspaceLoader, "startWorkspace").and.callFake(() => {
                done();
            });

            spyOn(workspaceLoader, "openIDE");

            workspaceLoader.load();
        });
        
        it('openIDE must not be called if status is STOPPED', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });
        
        it('must subscribe to events', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).toHaveBeenCalled();
        });
        
        it('must start the workspace', () => {
            expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
        });

        it('openIDE must be called when workspace become RUNNING', () => {
            workspaceLoader.onWorkspaceStatusChanged("RUNNING");
            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });
    });

    describe('must restart STOPPING workspace', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    fakeWorkspaceConfig.status = 'STOPPING';
                    resolve(fakeWorkspaceConfig);
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents").and.callFake(() => {
                return new Promise((resolve) => {
                    resolve();
                });
            });
    
            spyOn(workspaceLoader, "startWorkspace");
            spyOn(workspaceLoader, "openIDE");

            workspaceLoader.load().then(() => {
                done();
            });
        });

        it('must start the workspace after stopping', () => {
            expect(workspaceLoader.startAfterStopping).toEqual(true);
        });
        

        it('must start workspace when workspace status become STOPPED', () => {
            workspaceLoader.onWorkspaceStatusChanged("STOPPED");
            expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

        it('must open IDE when workspace become RUNNING', () => {
            workspaceLoader.onWorkspaceStatusChanged("RUNNING");
            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });
    });

});
