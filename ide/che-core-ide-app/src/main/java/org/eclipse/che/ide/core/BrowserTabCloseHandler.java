/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.core;

import static org.eclipse.che.ide.preferences.pages.general.IdeGeneralPreferencesPresenter.PREF_IDE_GENERAL_TAB_CLOSING;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.WindowActionEvent;
import org.eclipse.che.ide.api.WindowActionHandler;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;

/** Process browser tab leave events. */
public class BrowserTabCloseHandler implements WindowActionHandler {

  private final Provider<EditorAgent> editorAgentProvider;
  private final PreferencesManager preferencesManager;
  private final CoreLocalizationConstant localizationConstant;

  /** Is used to not to block programmatic close/reload of browser tab. */
  private boolean closeImmediately;

  @Inject
  public BrowserTabCloseHandler(
      EventBus eventBus,
      Provider<EditorAgent> editorAgentProvider,
      PreferencesManager preferencesManager,
      CoreLocalizationConstant localizationConstant) {
    this.editorAgentProvider = editorAgentProvider;
    this.preferencesManager = preferencesManager;
    this.localizationConstant = localizationConstant;

    closeImmediately = false;
    eventBus.addHandler(WindowActionEvent.TYPE, this);
  }

  /**
   * If true given then no checks will be performed before IDE leave.
   *
   * <p>Typically should be invoked before automatic page leave/reloading.
   */
  public void setCloseImmediately(boolean closeImmediately) {
    this.closeImmediately = closeImmediately;
  }

  @Override
  public void onWindowClosing(WindowActionEvent event) {
    if (!closeImmediately) {
      if (!isEditorsClean()) {
        event.setMessage(localizationConstant.changesMayBeLost());
      } else if (Boolean.parseBoolean(preferencesManager.getValue(PREF_IDE_GENERAL_TAB_CLOSING))) {
        event.setMessage(localizationConstant.closeTabConfirmation());
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
    return editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .noneMatch(EditorPartPresenter::isDirty);
  }
}
