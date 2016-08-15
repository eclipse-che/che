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

/**
 * Defines a way to grab remote ip
 * @author Florent Benoit
 */
export class RemoteIp {

    ip: string;

    constructor() {
        var execSync = require('child_process').execSync;
        this.ip = execSync('docker run --net host --rm codenvy/che-ip').toString().replace(/[\n\r]/g, '');
    }


   getIp() : string {
       return this.ip;
   }

}
