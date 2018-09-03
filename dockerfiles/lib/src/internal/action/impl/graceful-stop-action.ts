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
// imports
import {org} from "../../../api/dto/che-dto"
import {Parameter} from "../../../spi/decorator/parameter";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {Log} from "../../../spi/log/log";
import {System} from "../../../api/wsmaster/system/system";
/**
 * This class is handling the graceful stop command to the remote Che system
 * @author Florent Benoit
 */
export class GracefulStopAction {


    @Parameter({names: ["-s", "--url"], description: "Defines the url to be used"})
    url : string;

    @Parameter({names: ["-u", "--user"], description: "Defines the user to be used"})
    username : string;

    @Parameter({names: ["-w", "--password"], description: "Defines the password to be used"})
    password : string;

    args: Array<string>;
    authData: AuthData;

    fs = require('fs');
    path = require('path');

    system : System;

    constructor(args:Array<string>) {
        this.args = ArgumentProcessor.inject(this, args);
        this.authData = new AuthData(this.url, this.username, this.password);
        this.system = new System(this.authData);

    }

    run() : Promise<any> {
        // first, login
        return this.authData.login().then(() => {
            return this.system.gracefulStop();
        }).then((state:org.eclipse.che.api.system.shared.dto.SystemStateDto) => {
          Log.getLogger().info("Success: System state '" + state.getStatus() + "'.");
        });
    }



}
