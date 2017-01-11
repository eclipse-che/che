/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server.utils;

/**
 * Debug variable utilities.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgVariableUtils {

    public static final String[] SUPER_GLOBALS = new String[] { "$GLOBALS", "$_SERVER", "$_GET", "$_POST", "$_FILES",
            "$_COOKIE", "$_SESSION", "$_REQUEST", "$_ENV" };
    public static final String THIS = "$this";

    /**
     * Checks if variable name is 'this' variable name.
     * 
     * @param name
     * @return <code>true</code> if variable name is 'this' variable name,
     *         <code>false</code> otherwise
     */
    public static boolean isThis(String name) {
        return THIS.equalsIgnoreCase(name);
    }

    /**
     * Checks if variable name is one of the global variables.
     * 
     * @param name
     * @return <code>true</code> if variable name is global variable name,
     *         <code>false</code> otherwise
     */
    public static boolean isSuperGlobal(String name) {
        for (int i = 0; i < SUPER_GLOBALS.length; i++)
            if (SUPER_GLOBALS[i].equalsIgnoreCase(name))
                return true;
        return false;
    }

}
