/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.lang.os;

/**
 * Escapes Windows path to unix-style path.
 *
 * @author Alexander Garagatyi
 */
public class WindowsPathEscaper {

    /** Implements singleton pattern. */
    private static final WindowsPathEscaper escaper = new WindowsPathEscaper();

    /** Does nothing. Can be used for mocking purposes. */
    public WindowsPathEscaper() {}

    /**
     * Static version of method that escapes paths on Windows.
     * It is discouraged to use this method because it is too hard to mock it.
     * Use {@link #escapePath(String)} instead.
     *
     * @param path path on Windows. Can be unix-style path also.
     * @return unix-style path
     * @see #escapePath(String)
     */
    public static String escapePathStatic(String path) {
        return escaper.escapePath(path);
    }

    /**
     * Escapes Windows path to unix-style path.
     *
     * @param path path on Windows. Can be unix-style path also.
     * @return unix-style path
     */
    public String escapePath(String path) {
        String esc;
        if (path.indexOf(":") == 1) {
            // check and replace only occurrence of ":" after disk label on Windows host (e.g. C:/)
            // but keep other occurrences it can be marker for docker mount volumes
            // (e.g. /path/dir/from/host:/name/of/dir/in/container                                               )
            esc = path.replaceFirst(":", "").replace('\\', '/');
            esc = Character.toLowerCase(esc.charAt(0)) + esc.substring(1); //letter of disk mark must be lower case
        } else {
            esc = path.replace('\\', '/');
        }
        if (!esc.startsWith("/")) {
            esc = "/" + esc;
        }
        return esc;
    }
}
