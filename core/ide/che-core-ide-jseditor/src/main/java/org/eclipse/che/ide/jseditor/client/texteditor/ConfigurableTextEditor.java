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
package org.eclipse.che.ide.jseditor.client.texteditor;

import javax.validation.constraints.NotNull;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.keymap.Keybinding;

public interface ConfigurableTextEditor extends TextEditor {

    /**
     * Initializes this editor with the configuration and document provider/
     *
     * @param configuration
     *         the configuration of this editor.
     * @param notificationManager
     *         the manager that provides showing notifications
     */

    void initialize(@NotNull TextEditorConfiguration configuration,
                    @NotNull NotificationManager notificationManager);


    /**
     * Returns the text editor configuration that was used for initialization.
     * @return the text editor configuration
     */
    TextEditorConfiguration getConfiguration();

    /**
     * Add an editor-spcific key binding.
     * @param keybinding the key binding
     */
    void addKeybinding(Keybinding keybinding);
}
