/*
 * Copyright (c) 2016-2018 Red Hat, Inc.
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
 * SSh class allowing to manage all ssh operations
 * @author Florent Benoit
 */
export class Ssh {

    /**
     * Authentication data
     */
    authData:AuthData;

    constructor(authData:AuthData) {
        this.authData = authData;
    }


    /**
     * Gets ssh pair by service and name.
     *
     * @param service
     *         service name of ssh pair
     * @param name
     *         name of ssh pair
     * @return instance of ssh pair
     * @throws NotFoundException
     *         when ssh pair is not found
     * @throws ServerException
     *         when any other error occurs during ssh pair fetching
     */
    getPair(service: string, name: string):Promise<org.eclipse.che.api.ssh.shared.dto.SshPairDto> {
        let jsonRequest: HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/ssh/' + service + '/find?name=' + name, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.ssh.shared.dto.SshPairDtoImpl);
        });
    }

}
