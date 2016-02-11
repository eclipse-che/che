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
package org.eclipse.che.ide.jseditor.client.preference.keymaps;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import org.eclipse.che.ide.jseditor.client.keymap.KeymapValuesHolder;

/** View interface for the preference page for the editor keymap selection. */
public interface KeymapsPreferenceView extends View<KeymapsPreferenceView.ActionDelegate> {

    /**
     * Sets the holder for selected keymap values.
     *
     * @param valuesHolder holder for keymaps
     */
    void setKeymapValuesHolder(KeymapValuesHolder valuesHolders);

    /**
     * Refreshes the view.
     */
    void refresh();

    /** Action delegate for the keymap preference view. */
    public interface ActionDelegate {

        /**
         * Action triggered when an keymap is selected for an editor type.
         *
         * @param editorType the editor type
         * @param keymap the new keymap
         */
        void editorKeymapChanged(EditorType editorType, Keymap keymap);
    }

}
