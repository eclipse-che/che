/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
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

require('./style.css');

import { WebsocketClient } from './json-rpc/websocket-client';
import { CheJsonRpcMasterApi } from './json-rpc/che-json-rpc-master-api';
import { Loader } from './loader/loader'

const WEBSOCKET_CONTEXT = '/api/websocket';

declare const Keycloak: Function;
export class KeycloakLoader {
    /**
     * Load keycloak settings
     */
    public loadKeycloakSettings(): Promise<any> {
        const msg = "Cannot load keycloak settings. This is normal for single-user mode.";

        return new Promise((resolve, reject) => {
            if (window.parent && window.parent['_keycloak']) {
                window['_keycloak'] = window.parent['_keycloak'];
                resolve(window['_keycloak']);
                return;
            }
            try {
                const request = new XMLHttpRequest();

                request.onerror = request.onabort = function () {
                    reject(msg);
                };

                request.onload = () => {
                    if (request.status == 200) {
                        resolve(this.injectKeycloakScript(JSON.parse(request.responseText)));
                    } else {
                        reject(null);
                    }
                };

                const url = "/api/keycloak/settings";
                request.open("GET", url, true);
                request.send();
            } catch (e) {
                reject(msg + e.message);
            }
        });
    }

    /**
     * Injects keycloak javascript
     */
    private injectKeycloakScript(keycloakSettings: any): Promise<any> {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.type = 'text/javascript';
            (script as any).language = 'javascript';
            script.async = true;
            script.src = keycloakSettings['che.keycloak.js_adapter_url'];

            script.onload = () => {
                resolve(this.initKeycloak(keycloakSettings));
            };

            script.onerror = script.onabort = () => {
                reject('Cannot load ' + script.src);
            };

            document.head.appendChild(script);
        });
    }

    /**
     * Initialize keycloak and load the IDE
     */
    private initKeycloak(keycloakSettings: any): Promise<any> {
        return new Promise((resolve, reject) => {
            function keycloakConfig() {
                const theOidcProvider = keycloakSettings['che.keycloak.oidc_provider'];
                if (!theOidcProvider) {
                    return {
                        url: keycloakSettings['che.keycloak.auth_server_url'],
                        realm: keycloakSettings['che.keycloak.realm'],
                        clientId: keycloakSettings['che.keycloak.client_id']
                    };
                } else {
                    return {
                        oidcProvider: theOidcProvider,
                        clientId: keycloakSettings['che.keycloak.client_id']
                    };
                }
            }
            const keycloak = Keycloak(keycloakConfig());            

            window['_keycloak'] = keycloak;

            var useNonce;
            if (typeof keycloakSettings['che.keycloak.use_nonce'] === 'string') {
                useNonce = keycloakSettings['che.keycloak.use_nonce'].toLowerCase() === 'true';
            }
            window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);
            keycloak
                .init({
                    onLoad: 'login-required',
                    checkLoginIframe: false,
                    useNonce: useNonce,
                    scope: 'email profile',
                    redirectUri: keycloakSettings['che.keycloak.redirect_url.ide']
                })
                .success(() => {
                    resolve(keycloak);
                })
                .error(() => {
                    reject('[Keycloak] Failed to initialize Keycloak');
                });
        });
    }

}

export class WorkspaceLoader {

    workspace: che.IWorkspace;
    startAfterStopping = false;

    constructor(private readonly loader: Loader,
                private readonly keycloak?: any) {
        /** Ask dashboard to show the IDE. */
        window.parent.postMessage("show-ide", "*");
    }

    load(): Promise<void> {
        const workspaceKey = this.getWorkspaceKey();

        if (!workspaceKey || workspaceKey === "") {
            console.error("Workspace is not defined");
            return;
        }

        return this.getWorkspace(workspaceKey)
            .then((workspace) => {
                this.workspace = workspace;
                return this.handleWorkspace();
            })
            .then(() => this.openIDE())
            .catch(err => {
                console.error(err);
                this.loader.error(err);
                this.loader.hideLoader();
                this.loader.showReload();
            });
    }

    /**
     * Returns workspace key from current address or empty string when it is undefined.
     */
    getWorkspaceKey(): string {
        const result: string = window.location.pathname.substr(1);
        return result.substr(result.indexOf('/') + 1, result.length);
    }

    /**
     * Returns base websocket URL.
     */
    websocketBaseURL(): string {
        const wsProtocol = 'http:' === document.location.protocol ? 'ws' : 'wss';
        return wsProtocol + '://' + document.location.host;
    }

    /**
     * Returns query string.
     */
    getQueryString(): string {
        return location.search;
    }

    /**
     * Get workspace by ID.
     * 
     * @param workspaceId workspace id
     */
    getWorkspace(workspaceId: string): Promise<che.IWorkspace> {
        const request = new XMLHttpRequest();
        request.open("GET", '/api/workspace/' + workspaceId);
        return this.setAuthorizationHeader(request).then((xhr: XMLHttpRequest) => {
            return new Promise<che.IWorkspace>((resolve, reject) => {
                xhr.send();
                xhr.onreadystatechange = () => {
                    if (xhr.readyState !== 4) { return; }
                    if (xhr.status !== 200) {
                        const errorMessage = 'Failed to get the workspace: "' + this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            });
        });
    }

    /**
     * Start current workspace.
     */
    startWorkspace(): Promise<che.IWorkspace> {
        const request = new XMLHttpRequest();
        request.open("POST", `/api/workspace/${this.workspace.id}/runtime`);
        return this.setAuthorizationHeader(request).then((xhr: XMLHttpRequest) => {
            return new Promise<che.IWorkspace>((resolve, reject) => {
                xhr.send();
                xhr.onreadystatechange = () => {
                    if (xhr.readyState !== 4) { return; }
                    if (xhr.status !== 200) {
                        const errorMessage = 'Failed to start the workspace: "'  + this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            });
        });
    }

    getRequestErrorMessage(xhr: XMLHttpRequest): string {
        let errorMessage;
        try {
            const response = JSON.parse(xhr.responseText);
            errorMessage = response.message;
        } catch (e) { }

        if (errorMessage) {
            return errorMessage;
        }

        if (xhr.statusText) {
            return xhr.statusText;
        }

        return "Unknown error";
    }

    /**
     * Handles workspace status.
     */
    handleWorkspace(): Promise<void> {
        if (this.workspace.status === 'RUNNING') {
            return new Promise((resolve, reject) => {
                this.checkWorkspaceRuntime().then(resolve, reject);
            });
        } else if (this.workspace.status === 'STOPPING') {
            this.startAfterStopping = true;
        }

        const masterApiConnectionPromise = new Promise((resolve, reject) => {
            if (this.workspace.status === 'STOPPED') {
                this.startWorkspace().then(resolve, reject);
            } else {
                resolve();
            }
        }).then(() => {
            return this.connectMasterApi();
        });

        const runningOnConnectionPromise = masterApiConnectionPromise
            .then((masterApi: CheJsonRpcMasterApi) => {
                return new Promise((resolve, reject) => {
                    masterApi.addListener('open', () => {
                        this.checkWorkspaceRuntime().then(resolve, reject);
                    });
                });
            });

        const runningOnStatusChangePromise = masterApiConnectionPromise
            .then((masterApi: CheJsonRpcMasterApi) => {
                return this.subscribeWorkspaceEvents(masterApi);
            });

        return Promise.race([runningOnConnectionPromise, runningOnStatusChangePromise]);
    }

    /**
     * Shows environment outputs.
     * 
     * @param message output message
     */
    onEnvironmentOutput(message) : void {
        this.loader.log(message);
    }

    connectMasterApi(): Promise<CheJsonRpcMasterApi> {
        return new Promise((resolve, reject) => {
            const entryPoint = this.websocketBaseURL() + WEBSOCKET_CONTEXT;
            const master = new CheJsonRpcMasterApi(new WebsocketClient(), entryPoint, this);
            master.connect(entryPoint)
                .then(() => resolve(master))
                .catch((error: any) => reject(error));
        });
    }

    /**
     * Subscribes to the workspace events.
     */
    subscribeWorkspaceEvents(masterApi: CheJsonRpcMasterApi) : Promise<any> {
        return new Promise((resolve, reject) => {
            masterApi.subscribeEnvironmentOutput(this.workspace.id,
                (message: any) => this.onEnvironmentOutput(message.text));
            masterApi.subscribeInstallerOutput(this.workspace.id,
                 (message: any) => this.onEnvironmentOutput(message.text));
            masterApi.subscribeWorkspaceStatus(this.workspace.id,
                (message: any) => {
                    if (message.error) {
                        reject(new Error(`Failed to run the workspace: "${message.error}"`));
                    } else if (message.status === 'RUNNING') {
                        this.checkWorkspaceRuntime().then(resolve, reject);
                    } else if (message.status === 'STOPPED') {
                        this.startWorkspace().catch((error: any) => reject(error));
                    }
                });
        });
    }

    checkWorkspaceRuntime(): Promise<any> {
        return new Promise((resolve, reject) => {
            this.getWorkspace(this.workspace.id).then((workspace) => {
                if (workspace.status === 'RUNNING') {
                    if (workspace.runtime) {
                        resolve();
                    } else {
                        reject(new Error('You do not have permissions to access workspace runtime, in this case IDE cannot be loaded.'));
                    }
                }
            });
        });
    }

    /**
     * Opens IDE for the workspace.
     */
    openIDE() : void {
        this.getWorkspace(this.workspace.id).then((workspace) => {
            const machines = workspace.runtime.machines;
            for (const machineName in machines) {
                const servers = machines[machineName].servers;
                for (const serverId in servers) {
                    const attributes = servers[serverId].attributes;
                    if (attributes['type'] === 'ide') {
                        this.openURL(servers[serverId].url + this.getQueryString());
                        return;
                    }
                }
            }
            this.openURL(workspace.links.ide + this.getQueryString());
        });
    }

    /**
     * Schedule opening URL.
     * Scheduling prevents appearing an error net::ERR_CONNECTION_REFUSED instead opening the URL.
     * 
     * @param url url to be opened
     */
    openURL(url) : void {
        // Preconfigured IDE may use dedicated port. In this case Chrome browser fails
        // with error net::ERR_CONNECTION_REFUSED. Timer helps to open the URL without errors.
        setTimeout(() => {
            window.location.href = url;
        }, 100);
    }

    setAuthorizationHeader(xhr: XMLHttpRequest): Promise<XMLHttpRequest> {
        return new Promise((resolve, reject) => {
            if (this.keycloak && this.keycloak.token) {
                this.keycloak.updateToken(5).success(() => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + this.keycloak.token);
                    resolve(xhr);
                }).error(() => {
                    console.log('Failed to refresh token');
                    window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);
                    this.keycloak.login();
                    reject();
                });
            }

            resolve(xhr);
        });
    }

    getAuthenticationToken(): string {
        return this.keycloak && this.keycloak.token ? '?token=' + this.keycloak.token : '';
    }

}

/** Initialize */
if (document.getElementById('workspace-console')) {
    new KeycloakLoader().loadKeycloakSettings().catch((error: any) => {
        if (error) {
            console.log(error);
        }
    }).then((keycloak: any) => {
        new WorkspaceLoader(new Loader(), keycloak).load();
    });
}
