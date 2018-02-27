/// <reference path="../node_modules/@types/jasmine/index.d.ts" />
/// <reference path="../src/custom.d.ts" />

'use strict';

import {WorkspaceLoader} from '../src/index';
import { Loader } from '../src/loader/loader';

describe('Workspace Loader', () => {

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
    });

    it('> must have "workspace-loader" in DOM >', () => {
        const loader = document.getElementById('workspace-loader');
        expect(loader).toBeTruthy();
    });

    it('> test when workspace key is not specified', () => {
        let loader = new Loader();
        let workspaceLoader = new WorkspaceLoader(loader);

        spyOn(workspaceLoader, 'getWorkspaceKey');
        spyOn(workspaceLoader, 'getWorkspace');
        
        workspaceLoader.load();

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).not.toHaveBeenCalled();
    });

    it('> test getWorkspace with test value', () => {
        let loader = new Loader();
        let workspaceLoader = new WorkspaceLoader(loader);

        spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");

        spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
            return new Promise((resolve) => {
                const workspace = {status: 'STOPPED'} as che.IWorkspace;
                resolve(workspace);
            });
        });

        workspaceLoader.load();

        expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
        expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith("foo/bar");
    });

    describe('> must handle workspace', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    resolve(null);
                });
            });
    
            spyOn(workspaceLoader, "handleWorkspace").and.callFake(() => {
                done();
            });

            workspaceLoader.load();
        });

        it('> must be called', () => {
            expect(workspaceLoader.getWorkspaceKey).toHaveBeenCalled();
            expect(workspaceLoader.getWorkspace).toHaveBeenCalledWith("foo/bar");
            expect(workspaceLoader.handleWorkspace).toHaveBeenCalled();
        });
    });

    describe('> must open IDE for RUNNING workspace', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    const workspace = {
                        status: 'RUNNING'
                    } as che.IWorkspace;
    
                    resolve(workspace);
                });
            });

            spyOn(workspaceLoader, "subscribeWorkspaceEvents");
    
            spyOn(workspaceLoader, "openIDE").and.callFake(() => {
                done();
            });

            workspaceLoader.load();
        });

        it('> must be called', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).not.toHaveBeenCalled();
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
                    const workspace = {
                        status: 'STOPPED'
                    } as che.IWorkspace;
    
                    resolve(workspace);
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

        it('> must be called', () => {
            expect(workspaceLoader.subscribeWorkspaceEvents).toHaveBeenCalled();
            expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();

            workspaceLoader.onWorkspaceStatusChanged("RUNNING");

            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });
    });


    describe('> must restart STOPPING workspace', () => {
        let workspaceLoader;

        beforeEach((done) => {
            let loader = new Loader();
            workspaceLoader = new WorkspaceLoader(loader);
    
            spyOn(workspaceLoader, 'getWorkspaceKey').and.returnValue("foo/bar");
    
            spyOn(workspaceLoader, 'getWorkspace').and.callFake(() => {
                return new Promise((resolve) => {
                    const workspace = {
                        status: 'STOPPING'
                    } as che.IWorkspace;
    
                    resolve(workspace);
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

        it('> must be called', () => {
            expect(workspaceLoader.startAfterStopping).toEqual(true);

            workspaceLoader.onWorkspaceStatusChanged("STOPPED");

            expect(workspaceLoader.startWorkspace).toHaveBeenCalled();
            expect(workspaceLoader.openIDE).not.toHaveBeenCalled();

            workspaceLoader.onWorkspaceStatusChanged("RUNNING");

            expect(workspaceLoader.openIDE).toHaveBeenCalled();
        });
    });

});
