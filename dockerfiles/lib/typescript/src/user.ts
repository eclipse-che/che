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


import {AuthData} from "./auth-data";
import {UserDto} from "./dto/userdto";
import {Log} from "./log";
/**
 * Defines communication with remote User API
 * @author Florent Benoit
 */
export class User {


    /**
     * The HTTP library used to call REST API.
     */
    http: any;

    /**
     * Authentication data
     */
    authData : AuthData;


    constructor(authData : AuthData) {
        this.authData = authData;
        if (authData.isSecured()) {
            this.http = require('https');
        } else {
            this.http = require('http');
        }
    }


    /**
     * Create a user and return a promise with content of UserDto in case of success
     */
    createUser(name: string, email: string, password : string) : Promise<UserDto> {

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: '/api/user?token=' + this.authData.getToken(),
            method: 'POST',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        let p = new Promise<UserDto>( (resolve, reject) => {
            var req = this.http.request(options,  (res) => {

                var data: string = '';

                res.on('error',  (error) => {
                    Log.getLogger().error('rejecting as we got error', error);
                    reject('create user: Invalid response code' + res.statusCode + ':' + error);
                });

                res.on('data',  (body) => {
                    data += body;
                });

                res.on('end', () => {
                    if (res.statusCode == 201) {
                        resolve(new UserDto(JSON.parse(data)));
                    } else {
                        reject('create user: Invalid response code' + res.statusCode + ':' + data.toString());
                    }
                });
            });

            req.on('error', (err) => {
                Log.getLogger().error('rejecting as we got error', err);
                reject('HTTP error: ' + err);
            });


            var user = {
                password: password,
                name: name,
            };

            if (email) {
                user['email'] = email;
            }


            req.write(JSON.stringify(user));
            req.end();

        });
        return p;
    }

}
