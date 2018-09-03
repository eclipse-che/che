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
 * Provides data about ascii forms.
 * @author Florent Benoit
 */
export interface AsciiFormInfo {

    /**
     * Size of the column for the title (key)
     */
    getTitleColumnSize() : number;

    /**
     * Provides size of the column for the values
     */
    getValueColumnSize() : number;

    /**
     * If true, name of the properties need to be uppercase.
     */
    isUppercasePropertyName() : boolean;

}
