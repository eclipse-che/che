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
