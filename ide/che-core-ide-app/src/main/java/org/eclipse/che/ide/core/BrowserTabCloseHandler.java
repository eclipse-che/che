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
package org.eclipse.che.ide.core;

import static org.eclipse.che.ide.preferences.pages.general.IdeGeneralPreferencesPresenter.PREF_IDE_GENERAL_TAB_CLOSING;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.WindowActionEvent;
import org.eclipse.che.ide.api.WindowActionHandler;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;

/** Process browser tab leave events. */
public class BrowserTabCloseHandler implements WindowActionHandler {

  private final Provider<EditorAgent> editorAgentProvider;
  private final PreferencesManager preferencesManager;

  /** Is used when there is a need to close tab programmatically */
  private static boolean closeImmediately = false;

  public static void setCloseImmediately(boolean closeImmediately) {
    BrowserTabCloseHandler.closeImmediately = closeImmediately;
  }

  @Inject
  public BrowserTabCloseHandler(
      EventBus eventBus,
      Provider<EditorAgent> editorAgentProvider,
      PreferencesManager preferencesManager) {
    this.editorAgentProvider = editorAgentProvider;
    this.preferencesManager = preferencesManager;

    eventBus.addHandler(WindowActionEvent.TYPE, this);
  }

  @Override
  public void onWindowClosing(WindowActionEvent event) {
    if (!closeImmediately) {
      if (!isEditorsClean()) {
        event.setMessage("ask user");
      } else if (Boolean.parseBoolean(preferencesManager.getValue(PREF_IDE_GENERAL_TAB_CLOSING))) {
        event.setMessage("ask user");
      }
    }
  }

  @Override
  public void onWindowClosed(WindowActionEvent event) {
    // do nothing
  }

  /**
   * Checks whether content of all opened files is saved.
   *
   * @return true if no unsaved changes, false otherwise
   */
  private boolean isEditorsClean() {
    for (EditorPartPresenter editorPartPresenter : editorAgentProvider.get().getOpenedEditors()) {
      if (editorPartPresenter.isDirty()) {
        return false;
      }
    }

    return true;
  }
}
