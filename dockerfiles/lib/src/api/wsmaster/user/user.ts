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
import {org} from "../../dto/che-dto"
import {AuthData} from "../auth/auth-data";
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
    createUser(name: string, email: string, password : string) : Promise<org.eclipse.che.api.user.shared.dto.UserDto> {

        let userData = {
            password: password,
            name: name,
        };

        if (email) {
            userData['email'] = email;
        }
        let jsonRequest: HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/user', 201).setMethod('POST').setBody(userData);
        return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.user.shared.dto.UserDtoImpl);
        });
    }


    /**
     * Removes user based on given user id
     * @param userId the id (not email) of the user
     * @returns {Promise<UserDto>}
     */
    deleteUser(userId: string) : Promise<boolean> {
        let jsonRequest: HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/user/' + userId, 204).setMethod('DELETE');
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return true;
        });
    }

    /**
     * Search user by its username
     * @param username the name of the user (not the id)
     * @returns {Promise<UserDto>}
     */
    findUserName(username : string) : Promise<org.eclipse.che.api.user.shared.dto.UserDto> {
        let jsonRequest: HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/user/find?name=' + username, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.user.shared.dto.UserDtoImpl);
        });
    }





}
