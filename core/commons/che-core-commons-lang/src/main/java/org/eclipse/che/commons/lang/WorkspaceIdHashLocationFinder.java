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
package org.eclipse.che.commons.lang;

import java.io.File;

/**
 * Tool that calculates workspace files location on filesystem by the hash of workspace id.
 *
 * <p>We can have a lot of workspace and create root folder for all of them at the same level of filesystem
 * may be inefficient. Keep workspace root folder in some hierarchy tree, use hash code for it.
 * <br>Algorithm of evaluation path by workspace ID:
 * <li>Get hashcode of workspace ID
 * <li>For 3 lowest bytes starting from lowest
 * <li>Convert byte to HEX number
 * <li>Use it as parent folder for workspace
 * <br>Example:
 * <li>Let workspace ID:workspaceibi5g3ofwxl52qiq
 * <li>Get hashcode    :1100257838
 * <li>Binary version  :0b1000001_10010100_10011010_00101110
 * <li>Get 1st byte    :00101110
 * <li>Convert to HEX  :2E
 * <li>Get 2nd byte    :10011010
 * <li>Convert to HEX  :9A
 * <li>Get 3rd byte    :10010100
 * <li>Convert to HEX  :94
 * So 2E, 9A, 94 will be parents of workspace folder.
 * <br>Path to workspace: <provided root folder>/2e/9a/94/workspaceibi5g3ofwxl52qiq
 * <br>For workspace with id workspaceq23dfgh6543fh75t path will be
 * <provided root folder>/76/aa/81/workspaceq23dfgh6543fh75t
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class WorkspaceIdHashLocationFinder {
    private static final String[] segments = new String[] {
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
            "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
            "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
            "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
            "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
            "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
            "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
            "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
            "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df",
            "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
            "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff",
            };

    public static File calculateDirPath(File parent, String workspaceId) {
        final int hash = workspaceId.hashCode();
        final String relPath = segments[hash & 0xff] +
                               File.separatorChar + segments[(hash >> 8) & 0xff] +
                               File.separatorChar + segments[(hash >> 16) & 0xff] +
                               File.separatorChar + workspaceId;
        return new File(parent, relPath);
    }
}
