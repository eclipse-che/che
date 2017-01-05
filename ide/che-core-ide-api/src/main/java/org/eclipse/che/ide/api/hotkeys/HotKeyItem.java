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
package org.eclipse.che.ide.api.hotkeys;

/**
 * Representation hotKey which performs some action
 * @author Alexander Andrienko
 */
public class HotKeyItem {
    private String actionDescription;
    private String hotKey;

    public HotKeyItem(String actionDescription, String hotKey) {
        this.actionDescription = actionDescription;
        this.hotKey = hotKey;
    }

    /**
     * Get action description
     * @return action description
     */
    public String getActionDescription() {
        return actionDescription;
    }

    /**
     * Get hotKey
     * @return readable hotKey line
     */
    public String getHotKey() {
        return hotKey;
    }
}
