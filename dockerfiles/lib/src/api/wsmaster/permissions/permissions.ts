/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import {AuthData} from "../auth/auth-data";
import {DomainDto} from "./dto/domaindto";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";
import {PermissionDto} from "./dto/permissiondto";
/**
 * Defines communication with remote Permissions API
 * @author Florent Benoit
 */
export class Permissions {

    /**
     * Authentication data
     */
    authData:AuthData;


    constructor(authData:AuthData) {
        this.authData = authData;
    }


    /**
     * list all permissions
     */
    listPermissions():Promise<Array<DomainDto>> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/permissions', 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            let domainsDto:Array<DomainDto> = new Array<DomainDto>();
            JSON.parse(jsonResponse.getData()).forEach((entry)=> {
                domainsDto.push(new DomainDto(entry));
            });
            return domainsDto;
        });
    }


    /**
     * get permissions for a given domain
     */
    getPermission(domain:string):Promise<PermissionDto> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/permissions/' + domain, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return new PermissionDto(JSON.parse(jsonResponse.getData()));
        }, (error) => {
            return new PermissionDto({});
        });
    }

    updatePermissions(permissionDto:PermissionDto) {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/permissions', 204);
        return jsonRequest.setMethod('POST').setBody(permissionDto.getContent()).request().then((jsonResponse:HttpJsonResponse) => {
            return new PermissionDto(jsonResponse.getData());
        });
    }


    copyCurrentPermissionsToUser(newUserId:string):Promise<boolean> {
        return this.listPermissions().then(
            (domainsDto:Array<DomainDto>) => {
                let adminPermissionsPromises:Array<Promise<PermissionDto>> = new Array<Promise<PermissionDto>>();
                domainsDto.forEach((domain) => {
                    adminPermissionsPromises.push(this.getPermission(domain.getContent().id));
                });
                return Promise.all(adminPermissionsPromises);
            }
        ).then((adminsPermissions:Array<PermissionDto>) => {

            let updatedPermissionsPromises:Array<Promise<PermissionDto>> = new Array<Promise<PermissionDto>>();
            adminsPermissions.forEach((adminPermission:PermissionDto)=> {
                if (adminPermission.getContent().domain) {
                    // we replace the user by the new user
                    adminPermission.getContent().user = newUserId;
                    // update permissions
                    updatedPermissionsPromises.push(this.updatePermissions(adminPermission).then((updatedDto)=> {
                        return updatedDto;
                    }));
                }

            });

            return Promise.all(updatedPermissionsPromises);
        }).then(() => {
            return true;
        });
    }
}
