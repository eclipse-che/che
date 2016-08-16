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


// imports
import {AuthData} from "../auth-data";
import {Log} from "../log";
import {Parameter, ParameterType} from "../parameter/parameter";
import {ArgumentProcessor} from "../parameter/argument-processor";
import {User} from "../user";
import {Argument} from "../parameter/parameter";
import {Permissions} from "../permissions";
import {PermissionDto} from "../dto/permissiondto";
import {DomainDto} from "../dto/domaindto";

/**
 * This class is managing a post-check operation by creating a workspace, starting it and displaying the log data.
 * @author Florent Benoit
 */
export class AddUserAction {

    @Argument({description: "Name of the user to create"})
    userToAdd : string;

    @Argument({description: "Email of the user to create"})
    emailToAdd : string;

    @Argument({description: "Password of the user to create "})
    passwordToAdd : string;

    @Parameter({names: ["-s", "--url"], description: "Defines the url to be used"})
    url : string;

    @Parameter({names: ["-u", "--user"], description: "Defines the user to be used"})
    username : string;

    @Parameter({names: ["-w", "--password"], description: "Defines the password to be used"})
    password : string;

    authData: AuthData;
    user: User;

    constructor(args:Array<string>) {
        ArgumentProcessor.inject(this, args);
        this.authData = AuthData.parse(this.url, this.username, this.password);
        this.user = new User(this.authData);
    }

    run() : Promise<any> {
        // first, login
        return this.authData.login().then(() => {
            // then create user
            Log.getLogger().info('Creating user ' + this.userToAdd);
            return this.user.createUser(this.userToAdd, this.emailToAdd, this.passwordToAdd).then((userDto) => {
                Log.getLogger().info('User', this.userToAdd, 'created with id', userDto.getContent().id);

                // if admin, add all permissions
                let permissions: Permissions = new Permissions(this.authData);
                return permissions.listPermissions().then(
                    (domainsDto : Array<DomainDto>) => {
                        console.log('found domain', domainsDto);
                        let adminPermissionsPromises : Array<Promise<PermissionDto>>  = new Array<Promise<PermissionDto>>();


                        domainsDto.forEach((domain) => {
                            console.log('asking for permission for domain', domain.getContent().id);
                            adminPermissionsPromises.push(permissions.getPermission(domain.getContent().id));
                        });

                        return Promise.all(adminPermissionsPromises);

                    }
                ).then((adminsPermissions : Array<PermissionDto>) => {

                    let updatedPermissionsPromises : Array<Promise<PermissionDto>>  = new Array<Promise<PermissionDto>>();


                    console.log('we have resolved all permissions', adminsPermissions);
                    adminsPermissions.forEach((adminPermission : PermissionDto)=> {
                        console.log('checking adminPermission', adminPermission);
                        if (adminPermission.getContent().domain) {
                            console.log('there is domain in admin permission', adminPermission);

                            // we replace the user by the new user
                            adminPermission.getContent().user = userDto.getContent().id;
                            console.log('adminPermission user updated to', adminPermission);


                            // update permissions
                            updatedPermissionsPromises.push(permissions.updatePermissions(adminPermission).then((updatedDto)=> {
                                console.log('permission updated to', updatedDto);
                                return updatedDto;
                            }));
                        }

                    });

                    return Promise.all(updatedPermissionsPromises);

                }).then((data)=> {
                    console.log('finished with data', data);
                });




            })
        });
    }

}
