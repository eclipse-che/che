/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 1);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
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

Object.defineProperty(exports, "__esModule", { value: true });
var util_1 = __webpack_require__(7);
var JSON_RPC_VERSION = '2.0';
exports.CODE_REQUEST_TIMEOUT = 4000;
/**
 * This client is handling the JSON RPC requests, responses and notifications.
 *
 * @author Ann Shumilova
 */
var JsonRpcClient = /** @class */ (function () {
    function JsonRpcClient(client) {
        var _this = this;
        this.counter = 100;
        this.client = client;
        this.pendingRequests = new Map();
        this.notificationHandlers = new Map();
        this.client.addListener("message", function (message) {
            _this.processResponse(message);
        });
    }
    /**
     * Performs JSON RPC request.
     *
     * @param method method's name
     * @param params params
     * @returns {IPromise<any>}
     */
    JsonRpcClient.prototype.request = function (method, params) {
        var deferred = new util_1.Deffered();
        var id = (this.counter++).toString();
        this.pendingRequests.set(id, deferred);
        var request = {
            jsonrpc: JSON_RPC_VERSION,
            id: id,
            method: method,
            params: params
        };
        this.client.send(request);
        return deferred.promise;
    };
    /**
     * Sends JSON RPC notification.
     *
     * @param method method's name
     * @param params params (optional)
     */
    JsonRpcClient.prototype.notify = function (method, params) {
        var request = {
            jsonrpc: JSON_RPC_VERSION,
            method: method,
            params: params
        };
        this.client.send(request);
    };
    /**
     * Adds notification handler.
     *
     * @param method method's name
     * @param handler handler to process notification
     */
    JsonRpcClient.prototype.addNotificationHandler = function (method, handler) {
        var handlers = this.notificationHandlers.get(method);
        if (handlers) {
            handlers.push(handler);
        }
        else {
            handlers = [handler];
            this.notificationHandlers.set(method, handlers);
        }
    };
    /**
     * Removes notification handler.
     *
     * @param method method's name
     * @param handler handler
     */
    JsonRpcClient.prototype.removeNotificationHandler = function (method, handler) {
        var handlers = this.notificationHandlers.get(method);
        if (handlers) {
            handlers.splice(handlers.indexOf(handler), 1);
        }
    };
    /**
     * Processes response - detects whether it is JSON RPC response or notification.
     *
     * @param message
     */
    JsonRpcClient.prototype.processResponse = function (message) {
        if (message.id && this.pendingRequests.has(message.id)) {
            this.processResponseMessage(message);
        }
        else {
            this.processNotification(message);
        }
    };
    /**
     * Processes JSON RPC notification.
     *
     * @param message message
     */
    JsonRpcClient.prototype.processNotification = function (message) {
        var method = message.method;
        var handlers = this.notificationHandlers.get(method);
        if (handlers && handlers.length > 0) {
            handlers.forEach(function (handler) {
                handler(message.params);
            });
        }
    };
    /**
     * Process JSON RPC response.
     *
     * @param message
     */
    JsonRpcClient.prototype.processResponseMessage = function (message) {
        var promise = this.pendingRequests.get(message.id);
        if (message.result) {
            promise.resolve(message.result);
            return;
        }
        if (message.error) {
            promise.reject(message.error);
        }
    };
    return JsonRpcClient;
}());
exports.JsonRpcClient = JsonRpcClient;


/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
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

Object.defineProperty(exports, "__esModule", { value: true });
__webpack_require__(2);
var websocket_client_1 = __webpack_require__(3);
var che_json_rpc_master_api_1 = __webpack_require__(5);
var WEBSOCKET_CONTEXT = '/api/websocket';
var KeycloakLoader = /** @class */ (function () {
    function KeycloakLoader() {
    }
    /**
     * Load keycloak settings
     */
    KeycloakLoader.prototype.loadKeycloakSettings = function () {
        var _this = this;
        var msg = "Cannot load keycloak settings. This is normal for single-user mode.";
        return new Promise(function (resolve, reject) {
            if (window.parent && window.parent['_keycloak']) {
                window['_keycloak'] = window.parent['_keycloak'];
                resolve(window['_keycloak']);
                return;
            }
            try {
                var request_1 = new XMLHttpRequest();
                request_1.onerror = request_1.onabort = function () {
                    reject(msg);
                };
                request_1.onload = function () {
                    if (request_1.status == 200) {
                        resolve(_this.injectKeycloakScript(JSON.parse(request_1.responseText)));
                    }
                    else {
                        reject(null);
                    }
                };
                var url = "/api/keycloak/settings";
                request_1.open("GET", url, true);
                request_1.send();
            }
            catch (e) {
                reject(msg + e.message);
            }
        });
    };
    /**
     * Injects keycloak javascript
     */
    KeycloakLoader.prototype.injectKeycloakScript = function (keycloakSettings) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.language = 'javascript';
            script.async = true;
            script.src = keycloakSettings['che.keycloak.js_adapter_url'];
            script.onload = function () {
                resolve(_this.initKeycloak(keycloakSettings));
            };
            script.onerror = script.onabort = function () {
                reject('Cannot load ' + script.src);
            };
            document.head.appendChild(script);
        });
    };
    /**
     * Initialize keycloak and load the IDE
     */
    KeycloakLoader.prototype.initKeycloak = function (keycloakSettings) {
        return new Promise(function (resolve, reject) {
            function keycloakConfig() {
                var theOidcProvider = keycloakSettings['che.keycloak.oidc_provider'];
                if (!theOidcProvider) {
                    return {
                        url: keycloakSettings['che.keycloak.auth_server_url'],
                        realm: keycloakSettings['che.keycloak.realm'],
                        clientId: keycloakSettings['che.keycloak.client_id']
                    };
                }
                else {
                    return {
                        oidcProvider: theOidcProvider,
                        clientId: keycloakSettings['che.keycloak.client_id']
                    };
                }
            }
            var keycloak = Keycloak(keycloakConfig());
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
                .success(function () {
                resolve(keycloak);
            })
                .error(function () {
                reject('[Keycloak] Failed to initialize Keycloak');
            });
        });
    };
    return KeycloakLoader;
}());
exports.KeycloakLoader = KeycloakLoader;
var WorkspaceLoader = /** @class */ (function () {
    function WorkspaceLoader(keycloak) {
        this.keycloak = keycloak;
        this.startAfterStopping = false;
        /** Ask dashboard to show the IDE. */
        window.parent.postMessage("show-ide", "*");
    }
    WorkspaceLoader.prototype.load = function () {
        var _this = this;
        var workspaceKey = this.getWorkspaceKey();
        if (!workspaceKey || workspaceKey === "") {
            console.error("Workspace is not defined");
            return;
        }
        return this.getWorkspace(workspaceKey)
            .then(function (workspace) {
            _this.workspace = workspace;
            return _this.handleWorkspace();
        })
            .then(function () { return _this.openIDE(); })
            .catch(function (err) {
            console.error(err);
            // this.loader.error(err);
            //this.loader.hideLoader();
            //this.loader.showReload();
        });
    };
    /**
     * Returns workspace key from current address or empty string when it is undefined.
     */
    WorkspaceLoader.prototype.getWorkspaceKey = function () {
        var result = window.location.pathname.substr(1);
        return result;
        //.substr(result.indexOf('/') + 1, result.length);
    };
    /**
     * Returns base websocket URL.
     */
    WorkspaceLoader.prototype.websocketBaseURL = function () {
        var wsProtocol = 'http:' === document.location.protocol ? 'ws' : 'wss';
        return wsProtocol + '://' + document.location.host;
    };
    /**
     * Returns query string.
     */
    WorkspaceLoader.prototype.getQueryString = function () {
        return location.search;
    };
    /**
     * Get workspace by ID.
     *
     * @param workspaceId workspace id
     */
    WorkspaceLoader.prototype.getWorkspace = function (workspaceId) {
        var _this = this;
        var request = new XMLHttpRequest();
        request.open("GET", '/api/workspace/' + workspaceId);
        return this.setAuthorizationHeader(request).then(function (xhr) {
            return new Promise(function (resolve, reject) {
                xhr.send();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState !== 4) {
                        return;
                    }
                    if (xhr.status !== 200) {
                        var errorMessage = 'Failed to get the workspace: "' + _this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            });
        });
    };
    /**
     * Start current workspace.
     */
    WorkspaceLoader.prototype.startWorkspace = function () {
        var _this = this;
        var request = new XMLHttpRequest();
        request.open("POST", "/api/workspace/" + this.workspace.id + "/runtime");
        return this.setAuthorizationHeader(request).then(function (xhr) {
            return new Promise(function (resolve, reject) {
                xhr.send();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState !== 4) {
                        return;
                    }
                    if (xhr.status !== 200) {
                        var errorMessage = 'Failed to start the workspace: "' + _this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            });
        });
    };
    WorkspaceLoader.prototype.getRequestErrorMessage = function (xhr) {
        var errorMessage;
        try {
            var response = JSON.parse(xhr.responseText);
            errorMessage = response.message;
        }
        catch (e) { }
        if (errorMessage) {
            return errorMessage;
        }
        if (xhr.statusText) {
            return xhr.statusText;
        }
        return "Unknown error";
    };
    /**
     * Handles workspace status.
     */
    WorkspaceLoader.prototype.handleWorkspace = function () {
        var _this = this;
        if (this.workspace.status === 'RUNNING') {
            return new Promise(function (resolve, reject) {
                _this.checkWorkspaceRuntime().then(resolve, reject);
            });
        }
        else if (this.workspace.status === 'STOPPING') {
            this.startAfterStopping = true;
        }
        var masterApiConnectionPromise = new Promise(function (resolve, reject) {
            if (_this.workspace.status === 'STOPPED') {
                _this.startWorkspace().then(resolve, reject);
            }
            else {
                resolve();
            }
        }).then(function () {
            return _this.connectMasterApi();
        });
        var runningOnConnectionPromise = masterApiConnectionPromise
            .then(function (masterApi) {
            return new Promise(function (resolve, reject) {
                masterApi.addListener('open', function () {
                    _this.checkWorkspaceRuntime().then(resolve, reject);
                });
            });
        });
        var runningOnStatusChangePromise = masterApiConnectionPromise
            .then(function (masterApi) {
            return _this.subscribeWorkspaceEvents(masterApi);
        });
        return Promise.race([runningOnConnectionPromise, runningOnStatusChangePromise]);
    };
    /**
     * Shows environment outputs.
     *
     * @param message output message
     */
    WorkspaceLoader.prototype.onEnvironmentOutput = function (message) {
        console.log(message);
    };
    WorkspaceLoader.prototype.connectMasterApi = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            var entryPoint = _this.websocketBaseURL() + WEBSOCKET_CONTEXT + _this.getAuthenticationToken();
            var master = new che_json_rpc_master_api_1.CheJsonRpcMasterApi(new websocket_client_1.WebsocketClient(), entryPoint);
            master.connect(entryPoint)
                .then(function () { return resolve(master); })
                .catch(function (error) { return reject(error); });
        });
    };
    /**
     * Subscribes to the workspace events.
     */
    WorkspaceLoader.prototype.subscribeWorkspaceEvents = function (masterApi) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            masterApi.subscribeEnvironmentOutput(_this.workspace.id, function (message) { return _this.onEnvironmentOutput(message.text); });
            masterApi.subscribeInstallerOutput(_this.workspace.id, function (message) { return _this.onEnvironmentOutput(message.text); });
            masterApi.subscribeWorkspaceStatus(_this.workspace.id, function (message) {
                if (message.error) {
                    reject(new Error("Failed to run the workspace: \"" + message.error + "\""));
                }
                else if (message.status === 'RUNNING') {
                    _this.checkWorkspaceRuntime().then(resolve, reject);
                }
                else if (message.status === 'STOPPED') {
                    _this.startWorkspace().catch(function (error) { return reject(error); });
                }
            });
        });
    };
    WorkspaceLoader.prototype.checkWorkspaceRuntime = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            _this.getWorkspace(_this.workspace.id).then(function (workspace) {
                if (workspace.status === 'RUNNING') {
                    if (workspace.runtime) {
                        resolve();
                    }
                    else {
                        reject(new Error('You do not have permissions to access workspace runtime, in this case IDE cannot be loaded.'));
                    }
                }
            });
        });
    };
    /**
     * Opens IDE for the workspace.
     */
    WorkspaceLoader.prototype.openIDE = function () {
        var _this = this;
        this.getWorkspace(this.workspace.id).then(function (workspace) {
            var machines = workspace.runtime.machines;
            for (var machineName in machines) {
                var servers = machines[machineName].servers;
                for (var serverId in servers) {
                    var attributes = servers[serverId].attributes;
                    if (attributes['type'] === 'ide') {
                        _this.openURL(servers[serverId].url + _this.getQueryString());
                        return;
                    }
                }
            }
            //fall back to GWT IDE behavior
            //this.openURL(workspace.links.ide.replace('/workspace-loader','') + this.getQueryString());
            return;
        });
    };
    /**
     * Schedule opening URL.
     * Scheduling prevents appearing an error net::ERR_CONNECTION_REFUSED instead opening the URL.
     *
     * @param url url to be opened
     */
    WorkspaceLoader.prototype.openURL = function (url) {
        // Preconfigured IDE may use dedicated port. In this case Chrome browser fails
        // with error net::ERR_CONNECTION_REFUSED. Timer helps to open the URL without errors.
        setTimeout(function () {
            window.location.href = url;
        }, 100);
    };
    WorkspaceLoader.prototype.setAuthorizationHeader = function (xhr) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            if (_this.keycloak && _this.keycloak.token) {
                _this.keycloak.updateToken(5).success(function () {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + _this.keycloak.token);
                    resolve(xhr);
                }).error(function () {
                    console.log('Failed to refresh token');
                    window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);
                    _this.keycloak.login();
                    reject();
                });
            }
            resolve(xhr);
        });
    };
    WorkspaceLoader.prototype.getAuthenticationToken = function () {
        return this.keycloak && this.keycloak.token ? '?token=' + this.keycloak.token : '';
    };
    return WorkspaceLoader;
}());
exports.WorkspaceLoader = WorkspaceLoader;
new KeycloakLoader().loadKeycloakSettings().catch(function (error) {
    if (error) {
        console.log(error);
    }
}).then(function (keycloak) {
    new WorkspaceLoader(keycloak).load();
});


/***/ }),
/* 2 */
/***/ (function(module, exports) {

// removed by extract-text-webpack-plugin

/***/ }),
/* 3 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
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

Object.defineProperty(exports, "__esModule", { value: true });
var RWS = __webpack_require__(4);
/**
 * The implementation for JSON RPC protocol communication through websocket.
 *
 * @author Ann Shumilova
 */
var WebsocketClient = /** @class */ (function () {
    function WebsocketClient() {
        this.handlers = {};
    }
    /**
     * Performs connection to the pointed entrypoint.
     *
     * @param entrypoint the entrypoint to connect to
     */
    WebsocketClient.prototype.connect = function (entrypoint) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            _this.websocketStream = new RWS(entrypoint, [], {});
            _this.websocketStream.addEventListener("open", function (event) {
                var eventType = "open";
                _this.callHandlers(eventType, event);
                resolve();
            });
            _this.websocketStream.addEventListener("error", function (event) {
                var eventType = "error";
                _this.callHandlers(eventType, event);
                reject();
            });
            _this.websocketStream.addEventListener("message", function (message) {
                var data = JSON.parse(message.data);
                var eventType = "message";
                _this.callHandlers(eventType, data);
            });
            _this.websocketStream.addEventListener("close", function (event) {
                var eventType = "close";
                _this.callHandlers(eventType, event);
            });
        });
    };
    /**
     * Adds a listener on an event.
     *
     * @param {communicationClientEvent} event
     * @param {Function} handler
     */
    WebsocketClient.prototype.addListener = function (event, handler) {
        if (!this.handlers[event]) {
            this.handlers[event] = [];
        }
        this.handlers[event].push(handler);
    };
    /**
     * Removes a listener.
     *
     * @param {communicationClientEvent} eventType
     * @param {Function} handler
     */
    WebsocketClient.prototype.removeListener = function (eventType, handler) {
        if (!this.handlers[eventType] || !handler) {
            return;
        }
        var index = this.handlers[eventType].indexOf(handler);
        if (index === -1) {
            return;
        }
        this.handlers[eventType].splice(index, 1);
    };
    /**
     * Performs closing the connection.
     * @param {number} code close code
     */
    WebsocketClient.prototype.disconnect = function (code) {
        if (this.websocketStream) {
            this.websocketStream.close(code ? code : undefined);
        }
    };
    /**
     * Sends pointed data.
     *
     * @param data to be sent
     */
    WebsocketClient.prototype.send = function (data) {
        this.websocketStream.send(JSON.stringify(data));
    };
    WebsocketClient.prototype.callHandlers = function (event, data) {
        if (this.handlers[event] && this.handlers[event].length > 0) {
            this.handlers[event].forEach(function (handler) { return handler(data); });
        }
    };
    return WebsocketClient;
}());
exports.WebsocketClient = WebsocketClient;


/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

;
;
;
var isWebSocket = function (constructor) {
    return constructor && constructor.CLOSING === 2;
};
var isGlobalWebSocket = function () {
    return typeof WebSocket !== 'undefined' && isWebSocket(WebSocket);
};
var getDefaultOptions = function () { return ({
    constructor: isGlobalWebSocket() ? WebSocket : null,
    maxReconnectionDelay: 10000,
    minReconnectionDelay: 1500,
    reconnectionDelayGrowFactor: 1.3,
    connectionTimeout: 4000,
    maxRetries: Infinity,
    debug: false,
}); };
var bypassProperty = function (src, dst, name) {
    Object.defineProperty(dst, name, {
        get: function () { return src[name]; },
        set: function (value) { src[name] = value; },
        enumerable: true,
        configurable: true,
    });
};
var initReconnectionDelay = function (config) {
    return (config.minReconnectionDelay + Math.random() * config.minReconnectionDelay);
};
var updateReconnectionDelay = function (config, previousDelay) {
    var newDelay = previousDelay * config.reconnectionDelayGrowFactor;
    return (newDelay > config.maxReconnectionDelay)
        ? config.maxReconnectionDelay
        : newDelay;
};
var LEVEL_0_EVENTS = ['onopen', 'onclose', 'onmessage', 'onerror'];
var reassignEventListeners = function (ws, oldWs, listeners) {
    Object.keys(listeners).forEach(function (type) {
        listeners[type].forEach(function (_a) {
            var listener = _a[0], options = _a[1];
            ws.addEventListener(type, listener, options);
        });
    });
    if (oldWs) {
        LEVEL_0_EVENTS.forEach(function (name) {
            ws[name] = oldWs[name];
        });
    }
};
var ReconnectingWebsocket = function (url, protocols, options) {
    var _this = this;
    if (options === void 0) { options = {}; }
    var ws;
    var connectingTimeout;
    var reconnectDelay = 0;
    var retriesCount = 0;
    var shouldRetry = true;
    var savedOnClose = null;
    var listeners = {};
    // require new to construct
    if (!(this instanceof ReconnectingWebsocket)) {
        throw new TypeError("Failed to construct 'ReconnectingWebSocket': Please use the 'new' operator");
    }
    // Set config. Not using `Object.assign` because of IE11
    var config = getDefaultOptions();
    Object.keys(config)
        .filter(function (key) { return options.hasOwnProperty(key); })
        .forEach(function (key) { return config[key] = options[key]; });
    if (!isWebSocket(config.constructor)) {
        throw new TypeError('Invalid WebSocket constructor. Set `options.constructor`');
    }
    var log = config.debug ? function () {
        var params = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            params[_i] = arguments[_i];
        }
        return console.log.apply(console, ['RWS:'].concat(params));
    } : function () { };
    /**
     * Not using dispatchEvent, otherwise we must use a DOM Event object
     * Deferred because we want to handle the close event before this
     */
    var emitError = function (code, msg) { return setTimeout(function () {
        var err = new Error(msg);
        err.code = code;
        if (Array.isArray(listeners.error)) {
            listeners.error.forEach(function (_a) {
                var fn = _a[0];
                return fn(err);
            });
        }
        if (ws.onerror) {
            ws.onerror(err);
        }
    }, 0); };
    var handleClose = function () {
        log('handleClose', { shouldRetry: shouldRetry });
        retriesCount++;
        log('retries count:', retriesCount);
        if (retriesCount > config.maxRetries) {
            emitError('EHOSTDOWN', 'Too many failed connection attempts');
            return;
        }
        if (!reconnectDelay) {
            reconnectDelay = initReconnectionDelay(config);
        }
        else {
            reconnectDelay = updateReconnectionDelay(config, reconnectDelay);
        }
        log('handleClose - reconnectDelay:', reconnectDelay);
        if (shouldRetry) {
            setTimeout(connect, reconnectDelay);
        }
    };
    var connect = function () {
        if (!shouldRetry) {
            return;
        }
        log('connect');
        var oldWs = ws;
        var wsUrl = (typeof url === 'function') ? url() : url;
        ws = new config.constructor(wsUrl, protocols);
        connectingTimeout = setTimeout(function () {
            log('timeout');
            ws.close();
            emitError('ETIMEDOUT', 'Connection timeout');
        }, config.connectionTimeout);
        log('bypass properties');
        for (var key in ws) {
            // @todo move to constant
            if (['addEventListener', 'removeEventListener', 'close', 'send'].indexOf(key) < 0) {
                bypassProperty(ws, _this, key);
            }
        }
        ws.addEventListener('open', function () {
            clearTimeout(connectingTimeout);
            log('open');
            reconnectDelay = initReconnectionDelay(config);
            log('reconnectDelay:', reconnectDelay);
            retriesCount = 0;
        });
        ws.addEventListener('close', handleClose);
        reassignEventListeners(ws, oldWs, listeners);
        // because when closing with fastClose=true, it is saved and set to null to avoid double calls
        ws.onclose = ws.onclose || savedOnClose;
        savedOnClose = null;
    };
    log('init');
    connect();
    this.close = function (code, reason, _a) {
        if (code === void 0) { code = 1000; }
        if (reason === void 0) { reason = ''; }
        var _b = _a === void 0 ? {} : _a, _c = _b.keepClosed, keepClosed = _c === void 0 ? false : _c, _d = _b.fastClose, fastClose = _d === void 0 ? true : _d, _e = _b.delay, delay = _e === void 0 ? 0 : _e;
        log('close - params:', { reason: reason, keepClosed: keepClosed, fastClose: fastClose, delay: delay, retriesCount: retriesCount, maxRetries: config.maxRetries });
        shouldRetry = !keepClosed && retriesCount <= config.maxRetries;
        if (delay) {
            reconnectDelay = delay;
        }
        ws.close(code, reason);
        if (fastClose) {
            var fakeCloseEvent_1 = {
                code: code,
                reason: reason,
                wasClean: true,
            };
            // execute close listeners soon with a fake closeEvent
            // and remove them from the WS instance so they
            // don't get fired on the real close.
            handleClose();
            ws.removeEventListener('close', handleClose);
            // run and remove level2
            if (Array.isArray(listeners.close)) {
                listeners.close.forEach(function (_a) {
                    var listener = _a[0], options = _a[1];
                    listener(fakeCloseEvent_1);
                    ws.removeEventListener('close', listener, options);
                });
            }
            // run and remove level0
            if (ws.onclose) {
                savedOnClose = ws.onclose;
                ws.onclose(fakeCloseEvent_1);
                ws.onclose = null;
            }
        }
    };
    this.send = function (data) {
        ws.send(data);
    };
    this.addEventListener = function (type, listener, options) {
        if (Array.isArray(listeners[type])) {
            if (!listeners[type].some(function (_a) {
                var l = _a[0];
                return l === listener;
            })) {
                listeners[type].push([listener, options]);
            }
        }
        else {
            listeners[type] = [[listener, options]];
        }
        ws.addEventListener(type, listener, options);
    };
    this.removeEventListener = function (type, listener, options) {
        if (Array.isArray(listeners[type])) {
            listeners[type] = listeners[type].filter(function (_a) {
                var l = _a[0];
                return l !== listener;
            });
        }
        ws.removeEventListener(type, listener, options);
    };
};
module.exports = ReconnectingWebsocket;


/***/ }),
/* 5 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
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

Object.defineProperty(exports, "__esModule", { value: true });
var che_json_rpc_api_service_1 = __webpack_require__(6);
var json_rpc_client_1 = __webpack_require__(0);
var MasterChannels;
(function (MasterChannels) {
    MasterChannels[MasterChannels["ENVIRONMENT_OUTPUT"] = 'runtime/log'] = "ENVIRONMENT_OUTPUT";
    MasterChannels[MasterChannels["ENVIRONMENT_STATUS"] = 'machine/statusChanged'] = "ENVIRONMENT_STATUS";
    MasterChannels[MasterChannels["INSTALLER_OUTPUT"] = 'installer/log'] = "INSTALLER_OUTPUT";
    MasterChannels[MasterChannels["WORKSPACE_STATUS"] = 'workspace/statusChanged'] = "WORKSPACE_STATUS";
})(MasterChannels || (MasterChannels = {}));
var SUBSCRIBE = 'subscribe';
var UNSUBSCRIBE = 'unsubscribe';
/**
 * Client API for workspace master interactions.
 *
 * @author Ann Shumilova
 */
var CheJsonRpcMasterApi = /** @class */ (function () {
    function CheJsonRpcMasterApi(client, entryPoint) {
        var _this = this;
        this.checkingDelay = 10000;
        this.fetchingClientIdTimeout = 5000;
        this.cheJsonRpcApi = new che_json_rpc_api_service_1.CheJsonRpcApiClient(client);
        this.client = client;
        client.addListener('open', function () { return _this.onConnectionOpen(); });
        client.addListener('close', function (event) {
            switch (event.code) {
                case 1000: // normal close
                    break;
                default:
                    _this.connect(entryPoint);
            }
        });
    }
    CheJsonRpcMasterApi.prototype.addListener = function (eventType, handler) {
        this.client.addListener(eventType, handler);
    };
    CheJsonRpcMasterApi.prototype.removeListener = function (eventType, handler) {
        this.client.removeListener(eventType, handler);
    };
    CheJsonRpcMasterApi.prototype.onConnectionOpen = function () {
        var _this = this;
        if (this.checkingInterval) {
            clearInterval(this.checkingInterval);
            this.checkingInterval = undefined;
        }
        this.checkingInterval = setInterval(function () {
            var isAlive = false;
            var fetchClientPromise = new Promise(function (resolve) {
                _this.fetchClientId().then(function () {
                    isAlive = true;
                    resolve(isAlive);
                }, function () {
                    isAlive = false;
                    resolve(isAlive);
                });
            });
            // this is timeout of fetchClientId request
            var fetchClientTimeoutPromise = new Promise(function (resolve) {
                setTimeout(function () {
                    resolve(isAlive);
                }, _this.fetchingClientIdTimeout);
            });
            Promise.race([fetchClientPromise, fetchClientTimeoutPromise]).then(function (isAlive) {
                if (isAlive) {
                    return;
                }
                clearInterval(_this.checkingInterval);
                _this.checkingInterval = undefined;
                _this.client.disconnect(json_rpc_client_1.CODE_REQUEST_TIMEOUT);
            });
        }, this.checkingDelay);
    };
    /**
     * Opens connection to pointed entryPoint.
     *
     * @param {string} entryPoint
     * @returns {IPromise<IHttpPromiseCallbackArg<any>>}
     */
    CheJsonRpcMasterApi.prototype.connect = function (entryPoint) {
        var _this = this;
        if (this.clientId) {
            var clientId = "clientId=" + this.clientId;
            // in case of reconnection
            // we need to test entrypoint on existing query parameters
            // to add already gotten clientId
            if (/\?/.test(entryPoint) === false) {
                clientId = '?' + clientId;
            }
            else {
                clientId = '&' + clientId;
            }
            entryPoint += clientId;
        }
        return this.cheJsonRpcApi.connect(entryPoint).then(function () {
            return _this.fetchClientId();
        });
    };
    /**
     * Subscribes the environment output.
     *
     * @param workspaceId workspace's id
     * @param machineName machine's name
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.subscribeEnvironmentOutput = function (workspaceId, callback) {
        this.subscribe(MasterChannels.ENVIRONMENT_OUTPUT, workspaceId, callback);
    };
    /**
     * Un-subscribes the pointed callback from the environment output.
     *
     * @param workspaceId workspace's id
     * @param machineName machine's name
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.unSubscribeEnvironmentOutput = function (workspaceId, callback) {
        this.unsubscribe(MasterChannels.ENVIRONMENT_OUTPUT, workspaceId, callback);
    };
    /**
     * Subscribes the environment status changed.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.subscribeEnvironmentStatus = function (workspaceId, callback) {
        this.subscribe(MasterChannels.ENVIRONMENT_STATUS, workspaceId, callback);
    };
    /**
     * Un-subscribes the pointed callback from environment status changed.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.unSubscribeEnvironmentStatus = function (workspaceId, callback) {
        this.unsubscribe(MasterChannels.ENVIRONMENT_STATUS, workspaceId, callback);
    };
    /**
     * Subscribes on workspace agent output.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.subscribeInstallerOutput = function (workspaceId, callback) {
        this.subscribe(MasterChannels.INSTALLER_OUTPUT, workspaceId, callback);
    };
    /**
     * Un-subscribes from workspace agent output.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.unSubscribeInstallerOutput = function (workspaceId, callback) {
        this.unsubscribe(MasterChannels.INSTALLER_OUTPUT, workspaceId, callback);
    };
    /**
     * Subscribes to workspace's status.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    CheJsonRpcMasterApi.prototype.subscribeWorkspaceStatus = function (workspaceId, callback) {
        var statusHandler = function (message) {
            if (workspaceId === message.workspaceId) {
                callback(message);
            }
        };
        this.subscribe(MasterChannels.WORKSPACE_STATUS, workspaceId, statusHandler);
    };
    /**
     * Un-subscribes pointed callback from workspace's status.
     *
     * @param workspaceId
     * @param callback
     */
    CheJsonRpcMasterApi.prototype.unSubscribeWorkspaceStatus = function (workspaceId, callback) {
        this.unsubscribe(MasterChannels.WORKSPACE_STATUS, workspaceId, callback);
    };
    /**
     * Fetch client's id and stores it.
     *
     * @returns {IPromise<TResult>}
     */
    CheJsonRpcMasterApi.prototype.fetchClientId = function () {
        var _this = this;
        return this.cheJsonRpcApi.request('websocketIdService/getId').then(function (data) {
            _this.clientId = data[0];
        });
    };
    /**
     * Returns client's id.
     *
     * @returns {string} clinet connection identifier
     */
    CheJsonRpcMasterApi.prototype.getClientId = function () {
        return this.clientId;
    };
    /**
     * Performs subscribe to the pointed channel for pointed workspace's ID and callback.
     *
     * @param channel channel to un-subscribe
     * @param workspaceId workspace's id
     * @param callback callback
     */
    CheJsonRpcMasterApi.prototype.subscribe = function (channel, workspaceId, callback) {
        var method = channel.toString();
        var params = { method: method, scope: { workspaceId: workspaceId } };
        this.cheJsonRpcApi.subscribe(SUBSCRIBE, method, callback, params);
    };
    /**
     * Performs un-subscribe of the pointed channel by pointed workspace's ID and callback.
     *
     * @param channel channel to un-subscribe
     * @param workspaceId workspace's id
     * @param callback callback
     */
    CheJsonRpcMasterApi.prototype.unsubscribe = function (channel, workspaceId, callback) {
        var method = channel.toString();
        var params = { method: method, scope: { workspaceId: workspaceId } };
        this.cheJsonRpcApi.unsubscribe(UNSUBSCRIBE, method, callback, params);
    };
    return CheJsonRpcMasterApi;
}());
exports.CheJsonRpcMasterApi = CheJsonRpcMasterApi;


/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
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

Object.defineProperty(exports, "__esModule", { value: true });
var json_rpc_client_1 = __webpack_require__(0);
var IChannel = /** @class */ (function () {
    function IChannel() {
    }
    return IChannel;
}());
exports.IChannel = IChannel;
/**
 * Class for basic CHE API communication methods.
 *
 * @author Ann Shumilova
 */
var CheJsonRpcApiClient = /** @class */ (function () {
    function CheJsonRpcApiClient(client) {
        this.client = client;
        this.jsonRpcClient = new json_rpc_client_1.JsonRpcClient(client);
    }
    /**
     * Subscribe on the events from service.
     *
     * @param event event's name to subscribe
     * @param notification notification name to handle
     * @param handler event's handler
     * @param params params (optional)
     */
    CheJsonRpcApiClient.prototype.subscribe = function (event, notification, handler, params) {
        this.jsonRpcClient.addNotificationHandler(notification, handler);
        this.jsonRpcClient.notify(event, params);
    };
    /**
     * Unsubscribe concrete handler from events from service.
     *
     * @param event event's name to unsubscribe
     * @param notification notification name binded to the event
     * @param handler handler to be removed
     * @param params params (optional)
     */
    CheJsonRpcApiClient.prototype.unsubscribe = function (event, notification, handler, params) {
        this.jsonRpcClient.removeNotificationHandler(notification, handler);
        this.jsonRpcClient.notify(event, params);
    };
    /**
     * Connects to the pointed entrypoint
     *
     * @param entrypoint entrypoint to connect to
     * @returns {Promise<any>} promise
     */
    CheJsonRpcApiClient.prototype.connect = function (entrypoint) {
        return this.client.connect(entrypoint);
    };
    /**
     * Makes request.
     *
     * @param method
     * @param params
     * @returns {ng.IPromise<any>}
     */
    CheJsonRpcApiClient.prototype.request = function (method, params) {
        return this.jsonRpcClient.request(method, params);
    };
    return CheJsonRpcApiClient;
}());
exports.CheJsonRpcApiClient = CheJsonRpcApiClient;


/***/ }),
/* 7 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

Object.defineProperty(exports, "__esModule", { value: true });
var Deffered = /** @class */ (function () {
    function Deffered() {
        var _this = this;
        this.promise = new Promise(function (resolve, reject) {
            _this.resolve = resolve;
            _this.reject = reject;
        });
    }
    Deffered.prototype.resolve = function (value) {
        this.resolveF(value);
    };
    Deffered.prototype.reject = function (reason) {
        this.rejectF(reason);
    };
    return Deffered;
}());
exports.Deffered = Deffered;


/***/ })
/******/ ]);