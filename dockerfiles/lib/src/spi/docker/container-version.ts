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
/**
 * Run in the container the command to get image being launched so we can detect the version of the image
 * @author Florent Benoit
 */
export class ContainerVersion {

    static version: string;

    constructor() {
        if (!ContainerVersion.version) {
            let execSync = require('child_process').execSync;
            ContainerVersion.version = execSync("docker inspect --format='{{.Config.Image}}' `hostname` | cut -d : -f2 -s").toString().replace(/[\n\r]/g, '');
        }
    }


    getVersion() : string {
        return ContainerVersion.version;
    }

}
