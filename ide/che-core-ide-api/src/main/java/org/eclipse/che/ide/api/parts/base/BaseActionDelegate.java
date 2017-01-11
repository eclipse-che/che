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
package org.eclipse.che.ide.api.parts.base;

/**
 * Base interface for action delegates, provide method for minimizing part.
 *
 * @author Evgen Vidolob
 */
public interface BaseActionDelegate {

    /**
     * Toggles maximized state of the part.
     */
    void onToggleMaximize();

    /**
     * Minimizes the part.
     */
    void onMinimize();

    /**
     * Activate Part when clicking the mouse.
     * Is used when the Part contains frames and mouse events are blocked.
     */
    void onActivate();

    /**
     * Asks to display part toolbar menu.
     *
     * @param mouseX
     *          mouse left
     * @param mouseY
     *          mouse top
     */
    void onPartMenu(int mouseX, int mouseY);

}
