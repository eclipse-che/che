/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */

/**
 * Handle some operations on strings.
 * @author Florent Benoit
 */
export class StringUtils {

    /**
     * Check if the given value string i starting with searchString
     * @param value the original string
     * @param searchString the value to search
     * @returns {boolean} true if it starts with
     */
    static startsWith(value:string, searchString:string):boolean {
        return value.substr(0, searchString.length) === searchString;
    }

    /**
     * Remove from the given string all the given comments starting by #
     * @param value
     * @returns {string}
     */
    static removeSharpComments(value : string) : string {
        return value.replace(/^#.*/gm, '');
    }

    /**
     * If string literals es6 def for workspace, use raw strings to keep dockerfile content
     * @param value
     * @returns {string}
     */
    static keepWorkspaceRawStrings(value : string) : string {
        return value.replace(/^(workspace.)(.*)=\`(.*)$/gm, '$1$2=String.raw`$3');
    }

}

