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
package org.eclipse.che.ide.editor.preferences.keymaps;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.editor.keymap.Keymap;

import java.util.List;

/** View interface for the preference page for the editor keymap selection. */
public interface KeymapsPreferenceView extends View<KeymapsPreferenceView.ActionDelegate> {

    void setKeyBindings(List<Keymap> availableKeyBindings, Keymap currentKeyBinding);

    /** Action delegate for the keymap preference view. */
    interface ActionDelegate {

        /**
         * Action triggered when an keymap is selected.
         *
         * @param keymap
         *         the new keymap
         */
        void onKeyBindingSelected(Keymap keymap);
    }
}
