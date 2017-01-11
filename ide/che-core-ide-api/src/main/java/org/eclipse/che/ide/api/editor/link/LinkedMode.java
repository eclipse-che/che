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
package org.eclipse.che.ide.api.editor.link;

/**
 * Represents linked mode in editor.
 *
 * @author Evgen Vidolob
 */
public interface LinkedMode {

    /**
     * Starts Linked Mode, selects the first position and registers the listeners.
     *
     * @param model
     */
    void enterLinkedMode(LinkedModel model);

    /**
     * Exits Linked Mode. Optionally places the caret at linkedMode escapePosition.
     *
     * @param escapePosition
     *         if true, place the caret at the  escape position.
     */
    void exitLinkedMode(boolean escapePosition);

    /**
     * Exits Linked Mode. Optionally places the caret at linkedMode escapePosition.
     *
     * @param escapePosition
     *         if true, place the caret at the  escape position.
     * @param successful
     *         successful or not exit linked mode
     */
    void exitLinkedMode(boolean escapePosition, boolean successful);

    void selectLinkedGroup(int index);

    /**
     * Add listener for linked mode.
     *
     * @param listener
     */
    void addListener(LinkedModeListener listener);

    /**
     * Remove linked mode listener.
     *
     * @param listener
     */
    void removeListener(LinkedModeListener listener);

    /**
     * Listener for LinkedMode
     */
    interface LinkedModeListener {
        /**
         * Notifies when linked mode ended.
         *
         * @param successful
         *         true if user press "Enter", false otherwise.
         */
        void onLinkedModeExited(boolean successful, int start, int end);
    }

}
