/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */

import {RemoteIp} from "../../../spi/docker/remoteip";
import {Log} from "../../../spi/log/log";
import {ServerLocation} from "../../../utils/server-location";
import {error} from "util";
import {WsMasterLocation} from "../wsmaster-location";

/**
 * Defines a way to store the WS master location and the auth data in order to deal with remote REST or Websocket API
 * @author Florent Benoit
 * @author Oleksandr Garagatyi
 */
export class AuthData {


    private DEFAULT_TOKEN : string = '';
    private DEFAULT_HOSTNAME : string = new RemoteIp().getIp();
    private DEFAULT_MULTI_USER_PORT : number = 5050;

    printInfo : boolean = true;

    private token : string;
    private authServerLocation: ServerLocation;
    private username: string;
    private password: string;
    private authType: AUTH_TYPE;
    private wsMasterLocation: ServerLocation;

    constructor(cheMasterLocation?: string, username?: string, password?: string) {

        this.username = username;
        this.password = password;
        this.token = this.DEFAULT_TOKEN;
        this.wsMasterLocation = new WsMasterLocation(cheMasterLocation);

        // autodetect auth type
        if (!username && !password) {
            this.authType = AUTH_TYPE.NO;
        } else if (process.env.CHE_MULTIUSER)  {
            this.authType = AUTH_TYPE.KEYCLOAK;
            let authServer: string = process.env.CHE_KEYCLOAK_AUTH_SERVER_URL;
            if (authServer) {
                this.authServerLocation = ServerLocation.parse(authServer);
            } else {
                this.authServerLocation = new ServerLocation(this.DEFAULT_HOSTNAME, this.DEFAULT_MULTI_USER_PORT, false);
            }
        } else {
            this.authType = AUTH_TYPE.CODENVY_SSO;
            this.authServerLocation = this.wsMasterLocation;
        }
    }

    getToken() : string {
        return this.token;
    }

    getAuthorizationHeaderValue(): string {
        if (this.authType == AUTH_TYPE.KEYCLOAK) {
            return 'Bearer ' + this.token;
        } else {
            return this.token;
        }
    }

    getMasterLocation(): ServerLocation {
        return this.wsMasterLocation;
    }

    login() : Promise<boolean> {

        if (this.authType == AUTH_TYPE.NO) {
            return this.noLogin();
        }

        let http: any;
        let securedOrNot: string;
        if (this.authServerLocation.isSecure()) {
            http = require('https');
            securedOrNot = ' using SSL.';
        } else {
            http = require('http');
            securedOrNot = '.';
        }

        let logMessage: string = 'Authenticating ';
        if (this.username && this.password) {
            logMessage += 'as ' + this.username;
        }

        if (this.printInfo) {
            Log.getLogger().info(logMessage, 'on \"' + this.authServerLocation.getHostname() + ':' + this.authServerLocation.getPort() + '\"' + securedOrNot);
        }

        switch (this.authType) {
            case AUTH_TYPE.KEYCLOAK:
                return this.keyCloakLogin(http);
            case AUTH_TYPE.CODENVY_SSO:
                return this.ssoLogin(http);
            default:
                error('Che authentication type ' + this.authType + ' is illegal');
        }
    }

    private ssoLogin(http) : Promise<boolean> {
        return new Promise<any>((resolve, reject) => {
            let options = {
                hostname: this.authServerLocation.getHostname(),
                port: this.authServerLocation.getPort(),
                path: '/api/auth/login',
                method: 'POST',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/json;charset=UTF-8'
                }
            };
            let req = http.request(options, (res) => {
                res.on('data', (body) => {
                    if (res.statusCode == 200) {
                        // token get, continue
                        this.token = JSON.parse(body).value;
                        resolve(true);
                    } else {
                        // error
                        reject(body);
                    }
                });

            });

            req.on('error', (err) => {
                reject('HTTP error: ' + err);
            });

            const auth = {
                "username": this.username,
                "password": this.password
            };

            req.write(JSON.stringify(auth));
            req.end();

        });
    }

    private keyCloakLogin(http) : Promise<boolean> {
        return new Promise<any>((resolve, reject) => {
            let options = {
                hostname: this.authServerLocation.getHostname(),
                port: this.authServerLocation.getPort(),
                path: '/auth/realms/che/protocol/openid-connect/token',
                method: 'POST',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            };
            let req = http.request(options, (res) => {
                res.on('data', (body) => {
                    if (res.statusCode == 200) {
                        // token get, continue
                        this.token = JSON.parse(body).access_token;
                        resolve(true);
                    } else {
                        // error
                        reject(body);
                    }
                });

            });

            req.on('error', (err) => {
                reject('HTTP error: ' + err);
            });

            const auth = {
                "username": this.username,
                "password": this.password,
                "grant_type": "password",
                "client_id": "che-public"

            };
            let querystring = require('querystring');
            req.write(querystring.stringify(auth));
            req.end();
        });
    }

    private noLogin() : Promise<boolean> {
        return new Promise<any>((resolve) => {
            resolve(true);
        });
    }
}

enum AUTH_TYPE {
    KEYCLOAK,
    CODENVY_SSO,
    NO
}
