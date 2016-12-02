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
import {ContainerVersion} from "./container-version";

/**
 * Defines a way to grab remote ip
 * @author Florent Benoit
 */
export class RemoteIp {

    static ip: string;

    constructor() {
        if (!RemoteIp.ip) {
            var execSync = require('child_process').execSync;
            let containerVersion : string = new ContainerVersion().getVersion();
            RemoteIp.ip = execSync('docker run --net host --rm eclipse/che-ip:' + containerVersion).toString().replace(/[\n\r]/g, '');
        }
    }


   getIp() : string {
       return RemoteIp.ip;
   }

}
