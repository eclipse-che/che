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

import {Argument} from "../../../spi/decorator/parameter";
import {Parameter} from "../../../spi/decorator/parameter";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {User} from "../../../api/wsmaster/user/user";
import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {Log} from "../../../spi/log/log";
import {UserDto} from "../../../api/wsmaster/user/dto/userdto";
import {Permissions} from "../../../api/wsmaster/permissions/permissions";
/**
 * This class is handling the add of a user and also consider to add user as being admin.
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

    @Parameter({names: ["-a", "--admin"], description: "Grant admin role to the user"})
    admin : boolean;


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
            return this.user.createUser(this.userToAdd, this.emailToAdd, this.passwordToAdd).then((userDto : UserDto) => {
                Log.getLogger().info('User', this.userToAdd, 'created with id', userDto.getContent().id);

                // if user should not be addes as admin, job is done
                if (!this.admin) {
                    return Promise.resolve(true);
                } else {
                    let permissions: Permissions = new Permissions(this.authData);
                    return permissions.copyCurrentPermissionsToUser(userDto.getContent().id);
                }
            })
        });
    }

}
