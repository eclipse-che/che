/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.synchronization;

import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.FileContentUpdateEvent;
import org.eclipse.che.ide.resource.Path;

/**
 * Contains list of opened files with the same {@link Path} and listens to {@link
 * DocumentChangedEvent} and {@link FileContentUpdateEvent} to provide the synchronization of the
 * content for them.
 *
 * @author Roman Nikitenko
 */
public interface EditorGroupSynchronization {

  /**
   * Adds given editor in the group to sync its content.
   *
   * @param editor editor to sync content
   */
  void addEditor(EditorPartPresenter editor);

  /**
   * Removes given editor from the group and stops to track changes of content for this one.
   *
   * @param editor editor to remove from group
   */
  void removeEditor(EditorPartPresenter editor);

  /** Notify group that active editor is changed */
  void onActiveEditorChanged(@NotNull EditorPartPresenter activeEditor);

  /** Notify group that editor dirty state is changed */
  void onEditorDirtyStateChanged(@NotNull EditorPartPresenter changedEditor);

  /** Removes all editors from the group and stops to track changes of content for them. */
  void unInstall();

  /** Returns all editors for given group. */
  Set<EditorPartPresenter> getSynchronizedEditors();
}
