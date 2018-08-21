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
import {AuthData} from "../../api/wsmaster/auth/auth-data";
import {Log} from "../log/log";
import {ErrorMessage} from "../error/error-message";
import {ServerLocation} from "../../utils/server-location";
/**
 * Implementation of a Request on the remote server
 * @author Florent Benoit
 */
export class DefaultHttpJsonRequest implements HttpJsonRequest {

    body : any = {};
    /**
     * The HTTP library used to call REST API.
     */
    http: any;
    options: any;
    expectedStatusCode : number;


    constructor(authData : AuthData, server : ServerLocation, path : string, expectedStatusCode: number) {
        if (!server) {
            server = authData.getMasterLocation();
        }
        if (server.isSecure()) {
            this.http = require('https');
        } else {
            this.http = require('http');
        }
        this.expectedStatusCode = expectedStatusCode;

        this.options = {
            hostname: server.getHostname(),
            port: server.getPort(),
            path: path,
            method: 'GET',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };
        if (authData) {
            this.options.headers.Authorization = authData.getAuthorizationHeaderValue();
        }
    }


    setMethod(methodName: string) : DefaultHttpJsonRequest {
        this.options.method = methodName;
        return this;
    }


    setBody(value : any) : DefaultHttpJsonRequest {
        if (value.toJson) {
            this.body = value.toJson();
        } else {
            this.body = value;
        }
        return this;
    }


    request() : Promise<DefaultHttpJsonResponse> {
        Log.getLogger().debug('Request on ', this.options.hostname + ' with port ', this.options.port);
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
                                reject(new ErrorMessage('Call on rest url ' + this.options.path + ' returned invalid response code (' + res.statusCode + ') with error:' + parsed.message, res.statusCode));
                            } else {
                                reject(new ErrorMessage('Call on rest url ' + this.options.path + ' returned invalid response code (' + res.statusCode + ') with error:' + data, res.statusCode));
                            }
                        } catch (error) {
                            reject(new ErrorMessage('Call on rest url ' + this.options.path + ' returned invalid response code (' + res.statusCode + ') with error:' + data.toString(), res.statusCode));
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
            Log.getLogger().debug('Send request', this.options.path, 'with method', this.options.method, "using ip/port", this.options.hostname + ":" + this.options.port, ' body:', stringified);
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


    asDto(dtoImplementation : any) : any {
        //let interfaceName: string = dtoClass.name;
        return new dtoImplementation(JSON.parse(this.data));
    }

    asArrayDto(dtoImplementation : any) : Array<any> {
        let parsed : any = JSON.parse(this.data);
        let arrayDto:Array<any> = new Array<any>();
        parsed.forEach((entry) => {
            let implementationInstance = new dtoImplementation(entry);
            arrayDto.push(implementationInstance);
        });
        return arrayDto;
    }

}

export interface HttpJsonResponse {

    getData() : any;

    asDto(dtoImplementation : any) : any;

    asArrayDto(dtoImplementation : any) : Array<any>;

}

export interface HttpJsonRequest {

    setMethod(methodName: string) : DefaultHttpJsonRequest;

    setBody(body : any) : DefaultHttpJsonRequest;

    request() : Promise<DefaultHttpJsonResponse>;
}