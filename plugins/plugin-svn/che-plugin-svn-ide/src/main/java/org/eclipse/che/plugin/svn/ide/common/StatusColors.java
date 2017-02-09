/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.common;

import org.eclipse.che.ide.api.theme.Style;

/**
 * Provide standardized colors for SVN status
 */
public class StatusColors {

    private final String[][] statusColors;

    public StatusColors() {
        statusColors = new String[][] {
                {"M", Style.getVcsConsoleStagedFilesColor()},
                {"!", Style.getMainFontColor()},
                {"?", Style.getMainMenuFontSelectedColor()},
                {"A", Style.getVcsConsoleStagedFilesColor()},
                {"X", Style.getMainFontColor()},
                {"C", Style.getMainFontColor()},
                {"D", Style.getVcsConsoleUnstagedFilesColor()},
                {"+", Style.getVcsConsoleStagedFilesColor()},
                {"-", Style.getVcsConsoleUnstagedFilesColor()},
                {"@", Style.getVcsConsoleChangesLineNumbersColor()},
                {"U", Style.getVcsConsoleModifiedFilesColor()},
                {"G", Style.getVcsConsoleModifiedFilesColor()}
        };
    }

    /**
     * Provide the color defined for a given status
     *
     * @param statusPrefix
     *         the prefix describing the status
     * @return the color defined for the given status
     */
    public String getStatusColor(String statusPrefix) {
        String color = null;
        for (String[] stcol : statusColors) {
            if (stcol[0].equals(statusPrefix)) {
                // TODO: Turn the file paths into links (where appropriate)
                color = stcol[1];
                break;
            }
        }
        return color;
    }
}
