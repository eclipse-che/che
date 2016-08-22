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
/**
 * This class is handling the removal of a user
 * @author Florent Benoit
 */
export class RemoveUserAction {

    @Argument({description: "name of the user to remove"})
    usernameToDelete : string;

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
            Log.getLogger().info('Searching user with name ' + this.usernameToDelete);
            return this.user.findUserName(this.usernameToDelete).then((userDto: UserDto) => {
                // then delete user
                Log.getLogger().info('Removing user with name ' + this.usernameToDelete, 'and id', userDto.getId());
                return this.user.deleteUser(userDto.getId());
            });
        });
    }

}
