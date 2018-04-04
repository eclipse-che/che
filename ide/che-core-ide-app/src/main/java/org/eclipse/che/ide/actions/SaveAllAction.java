/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.util.loging.Log;

/** @author Evgen Vidolob */
@Singleton
public class SaveAllAction extends ProjectAction {

  private final EditorAgent editorAgent;

  @Inject
  public SaveAllAction(EditorAgent editorAgent, Resources resources) {
    super("Save All", "Save all changes for project", resources.save());
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    save(editorAgent.getOpenedEditors());
  }

  private void save(final List<EditorPartPresenter> editors) {
    if (editors.isEmpty()) {
      return;
    }

    final EditorPartPresenter editorPartPresenter = editors.get(0);
    if (editorPartPresenter.isDirty()) {
      editorPartPresenter.doSave(
          new AsyncCallback<EditorInput>() {
            @Override
            public void onFailure(Throwable caught) {
              Log.error(SaveAllAction.class, caught);
              // try to save other files
              editors.remove(editorPartPresenter);
              save(editors);
            }

            @Override
            public void onSuccess(EditorInput result) {
              editors.remove(editorPartPresenter);
              save(editors);
            }
          });
    } else {
      editors.remove(editorPartPresenter);
      save(editors);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void updateProjectAction(ActionEvent e) {
    boolean hasDirtyEditor = false;
    for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
      if (editor instanceof EditorWithAutoSave) {
        if (((EditorWithAutoSave) editor).isAutoSaveEnabled()) {
          continue;
        }
      }
      if (editor.isDirty()) {
        hasDirtyEditor = true;
        break;
      }
    }
    e.getPresentation().setEnabledAndVisible(hasDirtyEditor);
  }
}
