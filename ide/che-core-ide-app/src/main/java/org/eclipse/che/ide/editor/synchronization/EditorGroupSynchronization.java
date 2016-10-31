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
package org.eclipse.che.ide.editor.synchronization;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Contains list of opened files with the same {@link Path} and listens to {@link DocumentChangeEvent} and {@link FileContentUpdateEvent}
 * to provide the synchronization of the content for them.
 *
 * @author Roman Nikitenko
 */
public interface EditorGroupSynchronization {

    /**
     * Adds given editor in the group to sync its content.
     *
     * @param editor
     *         editor to sync content
     */
    void addEditor(EditorPartPresenter editor);

    /**
     * Removes given editor from the group and stops to track changes of content for this one.
     *
     * @param editor
     *         editor to remove from group
     */
    void removeEditor(EditorPartPresenter editor);

    /** Notify group that active editor has changed */
    void onActiveEditorChanged(@NotNull EditorPartPresenter activeEditor);

    /** Removes all editors from the group and stops to track changes of content for them. */
    void unInstall();

    /** Returns all editors for given group. */
    Set<EditorPartPresenter> getSynchronizedEditors();
}
