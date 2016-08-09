/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */

import {RemoteIp} from './remoteip';

/**
 * Defines a way to store the auth data in order to deal with remote REST or Websocket API
 * @author Florent Benoit
 */
export class AuthData {


    DEFAULT_TOKEN : string = '';
    DEFAULT_HOSTNAME : string = new RemoteIp().getIp();
    DEFAULT_PORT : number = 8080;


    hostname : string;
    port : number;
    token : string;
    secured : boolean;

    constructor(hostname?: string, port? : number, token? : string) {
        this.secured = false;
        if (hostname) {
            this.hostname = hostname;
        } else {
            this.hostname = this.DEFAULT_HOSTNAME;
        }

        if (port) {
            this.port = port;
        } else {
            this.port = this.DEFAULT_PORT;
        }


        if (token) {
            this.token = token;
        } else {
            this.token = this.DEFAULT_TOKEN;
        }

    }


    getHostname() : string {
        return this.hostname;
    }

    getPort() : number {
        return this.port;
    }

    isSecured() : boolean {
        return this.secured;
    }

    getToken(): string {
        return this.token;
    }


    initToken(login: string, password: string) {
        var http: any;
        if (this.isSecured()) {
            http = require('https');
        } else {
            http = require('http');
        }

        var options = {
            hostname: this.hostname,
            port: this.port,
            path: '/api/auth/login',
            method: 'POST',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        let p = new Promise<any>( (resolve, reject) => {
            var req = http.request(options,  (res) => {
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
                "username": login,
                "password": password
            };

            req.write(JSON.stringify(auth));
            req.end();

        });
        return p;


    }


    static parse(remoteUrl : string) : AuthData {
        // extract hostname and port
        const url = require('url');
        var urlObject : any = url.parse(remoteUrl);
        var port: number;
        var isSecured: boolean = false;
        // do we have a port ?
        if (urlObject && !urlObject.port) {
            if ('http:' === urlObject.protocol) {
                port = 80;
            } else if ('https:' === urlObject.protocol) {
                isSecured = true;
                port = 443;
            }
        } else {
            port = urlObject.port;
        }

        let authData: AuthData = new AuthData(urlObject.hostname, port);
        if (isSecured) {
            authData.secured = true;
        }
        return authData;
    }

}
