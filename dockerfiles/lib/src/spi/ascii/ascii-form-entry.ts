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
 * AsciiFormEntry is an entry to {@link AsciiForm}
 * @author Florent Benoit
 */
export class AsciiFormEntry {

    /**
     * Name of the entry (left display)
     */
    private name:string;

    /**
     * Value of the entry (right display)
     */
    private value:string;

    /**
     * Allow to build a new entry by specifying the name and value
     * @param name the key
     * @param value the value
     */
    public constructor(name:string, value:string) {
        this.name = name;
        this.value = value;
    }

    /**
     * Provides the value
     * @returns {string}
     */
    public getValue():string {
        return this.value;
    }


    /**
     * Provides the name
     * @returns {string}
     */
    public  getName():string {

        return this.name;
    }


    /**
     * Allow to compare one ascii form entry to another one
     */
    public compareTo(o:AsciiFormEntry):number {
        return this.name.localeCompare(o.getName());
    }

}