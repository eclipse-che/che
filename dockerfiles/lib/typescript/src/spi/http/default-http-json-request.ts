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

import {AuthData} from "../../api/wsmaster/auth/auth-data";
import {Log} from "../log/log";
/**
 * Implementation of a Request on the remote server
 * @author Florent Benoit
 */
export class DefaultHttpJsonRequest implements HttpJsonRequest {

    authData : AuthData;
    body : any = {};
    http: any;
    options: any;
    expectedStatusCode : number;


    constructor(authData : AuthData, url : string,  expectedStatusCode: number) {
        this.authData  = authData;
        if (authData.isSecured()) {
            this.http = require('https');
        } else {
            this.http = require('http');
        }
        this.expectedStatusCode = expectedStatusCode;

        this.options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: url,
            method: 'GET',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8',
                'Cookie': 'session-access-key=' + this.authData.getToken()
            }
        };

    }


    setMethod(methodName: string) : DefaultHttpJsonRequest {
        this.options.method = methodName;
        return this;
    }


    setBody(body : any) : DefaultHttpJsonRequest {
        this.body = body;
        return this;
    }


    request() : Promise<DefaultHttpJsonResponse> {

        return new Promise<DefaultHttpJsonResponse>( (resolve, reject) => {
            var req = this.http.request(this.options,  (res) => {

                var data: string = '';

                res.on('error',  (body)=> {
                    Log.getLogger().error('got the following error', body.toString());
                    reject(body);
                });

                res.on('data',  (body) => {
                    data += body;
                });

                res.on('end', () => {
                    Log.getLogger().debug('Reply for call', this.options.path, 'with method', this.options.method, 'statusCode:', res.statusCode, 'and got body:', data);

                    if (res.statusCode == this.expectedStatusCode) {
                        // workspace created, continue
                        resolve(new DefaultHttpJsonResponse(res.statusCode, data));
                    } else {
                        try {
                            var parsed = JSON.parse(data);
                            if (parsed.message) {
                                reject('Call on rest url ' + this.options.path + ' returned invalid response code (' + res.statusCode + ') with error:' + parsed.message);
                            } else {
                                reject('Call on rest url ' + this.options.path + ' returned invalid response code (' + res.statusCode + ') with error:' + data);
                            }
                        } catch (error) {
                            reject('Call on rest url ' + this.options.path + ' returned invalid response code (' + res.statusCode + ') with error:' + data.toString());
                        }

                    }
                });

            });

            req.on('error', (err) => {
                Log.getLogger().debug('http error using the following options', this.options, JSON.stringify(err));
                if (err.code && (err.code === 'ECONNREFUSED' || err.code === 'EHOSTUNREACH')) {
                    reject('Unable to connect to the remote host ' + this.options.hostname + ' on port ' + this.options.port
                        + '. Please check the server is listening and that there is no network issue to reach this host. Full error: ' + err);
                } else {
                    reject('HTTP error: ' + err);
                }
            });

            let stringified : string = JSON.stringify(this.body);
            Log.getLogger().debug('Send for request', this.options.path, 'with method', this.options.method, ' body:', stringified);
            req.write(stringified);
            req.end();

        });
    }


}


export class DefaultHttpJsonResponse implements HttpJsonResponse {

    responseCode : number;
    data : any;

    constructor (responseCode : number, data : any) {
        this.responseCode = responseCode;
        this.data = data;
    }

    getData() : any {
        return this.data;
    }
}

export interface HttpJsonResponse {

    getData() : any;
}

export interface HttpJsonRequest {

    setMethod(methodName: string) : DefaultHttpJsonRequest;

    setBody(body : any) : DefaultHttpJsonRequest;

    request() : Promise<DefaultHttpJsonResponse>;
}