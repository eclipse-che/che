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
 * Creates an helper class for building UUID (like for message bus channels, etc)
 * @author Florent Benoit
 */
export class UUID {
    static build() : string {
        var time = new Date().getTime();
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (match) => {
            var rem = (time + 16 * Math.random()) % 16 | 0;
            time = Math.floor(time / 16);
            return (match === 'x' ? rem : rem & 7 | 8).toString(16);
        });
    }

}