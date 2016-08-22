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


import {AuthData} from "../auth/auth-data";
import {UserDto} from "./dto/userdto";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";
/**
 * Defines communication with remote User API
 * @author Florent Benoit
 */
export class User {

    /**
     * Authentication data
     */
    authData : AuthData;


    constructor(authData : AuthData) {
        this.authData = authData;
    }


    /**
     * Create a user and return a promise with content of UserDto in case of success
     */
    createUser(name: string, email: string, password : string) : Promise<UserDto> {

        var userData = {
            password: password,
            name: name,
        };

        if (email) {
            userData['email'] = email;
        }
        var jsonRequest : HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/user', 201).setMethod('POST').setBody(userData);
        return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
            return new UserDto(JSON.parse(jsonResponse.getData()));
        });
    }


    /**
     * Removes user based on given user id
     * @param userId the id (not email) of the user
     * @returns {Promise<UserDto>}
     */
    deleteUser(userId: string) : Promise<boolean> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/user/' + userId, 204).setMethod('DELETE')
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return true;
        });
    }

    /**
     * Search user by its username
     * @param username the name of the user (not the id)
     * @returns {Promise<UserDto>}
     */
    findUserName(username : string) : Promise<UserDto> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/user/find?name=' + username, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return new UserDto(JSON.parse(jsonResponse.getData()));
        });
    }





}
