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

let loader: Loader;
let workspaceLoader: WorkspaceLoader;
let workspaceConfig: che.workspace.Workspace;
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

    workspaceConfig = {
        status: 'STOPPED',
        links: {
            ide: 'test url'
        },
        config: {
            defaultEnv: 'default',
            environments: {
                default: {
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

    loader = new Loader();
    workspaceLoader = new WorkspaceLoader(loader);
});

describe('workspace-loader', () => {

    it('should have "#workspace-loader" element in DOM', () => {
        const loaderElement = document.getElementById('workspace-loader');
        expect(loaderElement).toBeTruthy();
    });
});

describe('If workspace key is not specified then workspace-loader', () => {

    it('should not get a workspace', done => {
        spyOn(workspaceLoader, 'getWorkspaceKey');
        spyOn(workspaceLoader, 'getWorkspace');

        workspaceLoader.load().then(done);

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).not.toHaveBeenCalled();
    });

});

describe('If workspace key is specified then workspace-loader', () => {

    it('should get a workspace', done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                resolve(workspaceConfig);
            }));

        workspaceLoader.load().then(done);

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith('foo/bar');
    });

});

describe('If workspace has a pre-configured IDE with query parameters then workspace-loader', () => {
    const ideURL = 'ide URL';

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getQueryString').and.returnValue('?param=value');

        workspaceConfig.status = 'RUNNING';
        workspaceConfig.runtime = {
            machines: {
                ide: {
                    servers: {
                        server1: {
                            attributes: { type: 'ide' },
                            url: ideURL
                        }
                    }
                }
            }
        } as che.workspace.Runtime;
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                resolve(workspaceConfig);
            }));

        spyOn(workspaceLoader, 'openIDE').and.callThrough();
        spyOn(workspaceLoader, 'openURL');
        workspaceLoader.load().then(done);
    });

    it('should call openURL method with correct parameter', () => {
        expect(workspaceLoader.openURL).toHaveBeenCalledWith(ideURL + '?param=value');
    });

});

describe('If an IDE server is not defined in workspace config then workspace-loader', () => {

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getQueryString').and.returnValue('');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'RUNNING';
                workspaceConfig.config!.environments!['default'].machines = {};
                workspaceConfig.runtime = {} as che.workspace.Runtime;
                resolve(workspaceConfig);
            }));

        spyOn(workspaceLoader, 'openIDE').and.callThrough();
        spyOn(workspaceLoader, 'openURL');
        workspaceLoader.load().then(done);
    });

    it('should open IDE directly', () => {
        expect(workspaceLoader.openURL).toHaveBeenCalledWith(workspaceConfig.links!.ide);
    });
});

describe('If workspace status is STOPPING then workspace-loader', () => {

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STOPPING';
                resolve(workspaceConfig);
            }));
        spyOn(<any>workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());
        spyOn(<any>workspaceLoader, 'connectMasterApi').and.callFake(() => {
            done();
            return Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: () => { }
            });
        });
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoader.load();
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

});

describe('If workspace status is STOPPED then workspace-loader', () => {

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STOPPED';
                resolve(workspaceConfig);
            }));

        spyOn((<any>workspaceLoader), 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => {
            done();
            return Promise.resolve();
        });
        spyOn((<any>workspaceLoader), 'connectMasterApi').and.callFake(() =>
            Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: () => { }
            }));
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoader.load();
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

    it('should subscribe to workspace events', () => {
        expect((<any>workspaceLoader).subscribeWorkspaceEvents).toHaveBeenCalled();
    });

    it('should start the workspace', () => {
        expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
    });

});

// test intensional stopping of a workspace
describe.only('If workspace status is changed from STARTING to STOPPING then workspace-loader', () => {
    let statusChangeCallback: (event: che.workspace.event.WorkspaceStatusEvent) => {};
    const statusStoppingEvent: che.workspace.event.WorkspaceStatusEvent = {
        status: 'STOPPING',
        prevStatus: 'STARTING'
    };

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STARTING';
                resolve(workspaceConfig);
            }));
        spyOn(<any>workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());
        spyOn(<any>workspaceLoader, 'connectMasterApi').and.callFake(() => {
            done();
            return Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: (_workspaceId: string, callback: any) => {
                    statusChangeCallback = callback;
                }
            });
        });
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoader.load();
    });

    beforeEach(() => {
        spyOn((<any>workspaceLoader), 'onWorkspaceStatus').and.callThrough();

        statusChangeCallback(statusStoppingEvent);
        statusChangeCallback(statusStoppedEvent);
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

    it('should handle workspace status change', () => {
        expect((<any>workspaceLoader).onWorkspaceStatus).toHaveBeenCalledWith(statusStoppingEvent);
    });

    const statusStoppedEvent: che.workspace.event.WorkspaceStatusEvent = {
        status: 'STOPPED',
        prevStatus: 'STOPPING'
    };
    it.only('should not start the workspace', () => {
        expect(workspaceLoader.startWorkspace).not.toHaveBeenCalled();
    });
});

describe('If workspace status is changed from STOPPING to STOPPED then workspace-loader', () => {
    let statusChangeCallback: (event: che.workspace.event.WorkspaceStatusEvent) => {};
    const statusStoppedEvent: che.workspace.event.WorkspaceStatusEvent = {
        status: 'STOPPED',
        prevStatus: 'STOPPING'
    };

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STOPPING';
                resolve(workspaceConfig);
            }));
        spyOn(<any>workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());
        spyOn(<any>workspaceLoader, 'connectMasterApi').and.callFake(() => {
            done();
            return Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: (_workspaceId: string, callback: any) => {
                    statusChangeCallback = callback;
                }
            });
        });
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoader.load();
    });

    beforeEach(() => {
        spyOn((<any>workspaceLoader), 'onWorkspaceStatus').and.callThrough();

        statusChangeCallback(statusStoppedEvent);
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

    it('should handle workspace status change', () => {
        expect((<any>workspaceLoader).onWorkspaceStatus).toHaveBeenCalledWith(statusStoppedEvent);
    });

    it('should start a workspace', () => {
        expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
    });

});

describe('If workspace status is STARTING then workspace-loader', () => {

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STARTING';
                resolve(workspaceConfig);
            }));
        spyOn(<any>workspaceLoader, 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());
        spyOn(<any>workspaceLoader, 'connectMasterApi').and.callFake(() => {
            done();
            return Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: () => { }
            });
        });
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoader.load();
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

});

describe('If websocket connection to master API is established when workspace is not in RUNNING state then workspace-loader', () => {
    let onOpenCallback: Function;

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STOPPED';
                resolve(workspaceConfig);
            }));

        spyOn((<any>workspaceLoader), 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => {
            done();
            return Promise.resolve();
        });
        spyOn((<any>workspaceLoader), 'connectMasterApi').and.callFake(() =>
            Promise.resolve({
                addListener: (_eventName: string, callback: any) => {
                    onOpenCallback = callback;
                },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: () => { }
            }));
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoader.load();
    });

    beforeEach(done => {
        onOpenCallback();
        setTimeout(done, 1000);
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

});

describe('If workspace status is changed to RUNNING then workspace-loader', () => {
    let statusChangeCallback: (event: che.workspace.event.WorkspaceStatusEvent) => {};
    let workspaceLoaderPromise: Promise<void>;

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STARTING';
                resolve(workspaceConfig);
            }));
        spyOn((<any>workspaceLoader), 'connectMasterApi').and.callFake(() => {
            done();
            return Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: (_workspaceId: string, callback: any) => {
                    statusChangeCallback = callback;
                }
            });
        });
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        workspaceLoaderPromise = workspaceLoader.load();
    });

    beforeEach(done => {
        spyOn((<any>workspaceLoader), 'onWorkspaceStatus').and.callThrough();

        (workspaceLoader.getWorkspace as any).and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'RUNNING';
                workspaceConfig.runtime = {} as che.workspace.Runtime;
                resolve(workspaceConfig);
            }));

        statusChangeCallback({ status: 'RUNNING' });
        workspaceLoaderPromise.then(done);
    });

    it('should handle workspace status change', () => {
        expect((<any>workspaceLoader).onWorkspaceStatus).toHaveBeenCalledWith({ status: 'RUNNING' });
    });

    it('should open an IDE', () => {
        expect(workspaceLoader.openIDE).toHaveBeenCalled();
    });

});

describe('If workspace status is RUNNING', () => {

    beforeEach(() => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn((<any>workspaceLoader), 'connectMasterApi');
        spyOn((<any>workspaceLoader), 'subscribeWorkspaceEvents');
        spyOn(workspaceLoader, 'openIDE');
    });

    describe('and user owns the workspace then workspace-loader', () => {

        beforeEach(done => {
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    workspaceConfig.status = 'RUNNING';
                    workspaceConfig.runtime = {} as che.workspace.Runtime;
                    resolve(workspaceConfig);
                }));

            workspaceLoader.load().then(done);
        });

        it('should not connect to workspace master API', () => {
            expect((<any>workspaceLoader).connectMasterApi).not.toHaveBeenCalled();
        });

        it('should not subscribe to workspace events', () => {
            expect((<any>workspaceLoader).subscribeWorkspaceEvents).not.toHaveBeenCalled();
        });

        it('should open IDE immediately', () => {
            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });

    });

    describe('and user has not been granted permissions for shared workspace then workspace-loader', () => {

        beforeEach(done => {
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
                new Promise(resolve => {
                    workspaceConfig.status = 'RUNNING';
                    resolve(workspaceConfig);
                }));

            workspaceLoader.load().then(done);
        });

        it('should not connect to workspace master API', () => {
            expect((<any>workspaceLoader).connectMasterApi).not.toHaveBeenCalled();
        });

        it('should not subscribe to workspace events', () => {
            expect((<any>workspaceLoader).subscribeWorkspaceEvents).not.toHaveBeenCalled();
        });

        it('should not open an IDE', () => {
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
        });

    });
});

describe('If workspace fails to start then workspace-loader', () => {

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STOPPED';
                resolve(workspaceConfig);
            }));
        spyOn((<any>workspaceLoader), 'connectMasterApi').and.callFake(() =>
            Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: () => { }
            }));
        spyOn((<any>workspaceLoader), 'subscribeWorkspaceEvents').and.callThrough();
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.reject('Failed to start the workspace'));
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        spyOn(loader, 'error');
        spyOn(loader, 'hideLoader').and.callThrough();
        spyOn(loader, 'showReload').and.callThrough();

        workspaceLoader.load().then(done);
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

    it('should log an error', () => {
        expect(loader.error).toHaveBeenCalled();
    });

    testLoaderIsHidden();
    testProgressBarIsHidden();
    testPromptIsShown();

});

describe('If workspace gets an error status on start then workspace-loader', () => {
    let statusChangeCallback: (event: che.workspace.event.WorkspaceStatusEvent) => {};
    let workspaceLoadPromise: Promise<any>;
    const statusErrorEvent: che.workspace.event.WorkspaceStatusEvent = {
        error: 'Something bad happened.'
    };

    beforeEach(done => {
        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue('foo/bar');
        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() =>
            new Promise(resolve => {
                workspaceConfig.status = 'STOPPED';
                resolve(workspaceConfig);
            }));
        spyOn(workspaceLoader, 'startWorkspace').and.callFake(() => Promise.resolve());
        spyOn((<any>workspaceLoader), 'connectMasterApi').and.callFake(() => {
            done();
            return Promise.resolve({
                addListener: () => { },
                subscribeEnvironmentOutput: () => { },
                subscribeInstallerOutput: () => { },
                subscribeWorkspaceStatus: (_workspaceId: string, callback: any) => {
                    statusChangeCallback = callback;
                }
            });
        });
        spyOn((<any>workspaceLoader), 'onWorkspaceStatus').and.callThrough();
        spyOn(workspaceLoader, 'openIDE').and.callFake(() => Promise.resolve());

        spyOn(loader, 'error');
        spyOn(loader, 'hideLoader').and.callThrough();
        spyOn(loader, 'showReload').and.callThrough();

        workspaceLoadPromise = workspaceLoader.load();
    });

    beforeEach(done => {
        statusChangeCallback(statusErrorEvent);

        workspaceLoadPromise.then(done);
    });

    it('should handle workspace status change', () => {
        expect((<any>workspaceLoader).onWorkspaceStatus).toHaveBeenCalledWith(statusErrorEvent);
    });

    it('should not open an IDE', () => {
        expect(workspaceLoader.openIDE).not.toHaveBeenCalled();
    });

    it('should log an error', () => {
        expect(loader.error).toHaveBeenCalled();
    });

    testLoaderIsHidden();
    testProgressBarIsHidden();
    testPromptIsShown();

});

function testLoaderIsHidden() {
    it('should hide loader', () => {
        const workspaceLoaderLabel = document.getElementById('workspace-loader-label');

        expect(workspaceLoaderLabel).toBeTruthy();
        expect(workspaceLoaderLabel!.style.display).toEqual('none');
    });
}

function testProgressBarIsHidden() {
    it('should hide loader and progress bar', () => {
        const workspaceLoaderProgress = document.getElementById('workspace-loader-progress');
        if (!workspaceLoaderProgress) {
            fail('Failed to get workspace-loader-progress element');
            return;
        }

        expect(workspaceLoaderProgress.style.display).toBeTruthy();
        expect(workspaceLoaderProgress.style.display).toEqual('none');
    });
}

function testPromptIsShown() {
    it('should show message with "try again" prompt', () => {
        const workspaceLoaderReload = document.getElementById('workspace-loader-reload');

        expect(workspaceLoaderReload).toBeTruthy();
        expect(workspaceLoaderReload!.style.display).not.toEqual('none');
    });
}
