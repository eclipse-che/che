/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ToggleAction;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

/**
 * The Project Explorer view has a Link with Editor feature. This can be enabled in header of the
 * Project Explorer view by choosing Link with editor button. If Link wih Editor is enabled - the
 * current file open in the Editor will be highlighted in Project Explorer.
 */
@Singleton
public class LinkWithEditorAction extends ToggleAction implements ActivePartChangedHandler {

  public static final String LINK_WITH_EDITOR = "linkWithEditor";

  private final Provider<EditorAgent> editorAgentProvider;
  private final EventBus eventBus;
  private final PreferencesManager preferencesManager;

  private PartPresenter activePart;

  @Inject
  public LinkWithEditorAction(
      CoreLocalizationConstant localizationConstant,
      Provider<EditorAgent> editorAgentProvider,
      EventBus eventBus,
      PreferencesManager preferencesManager) {
    super(localizationConstant.actionLinkWithEditor());

    this.editorAgentProvider = editorAgentProvider;
    this.eventBus = eventBus;
    this.preferencesManager = preferencesManager;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation().setEnabledAndVisible(activePart instanceof ProjectExplorerPresenter);
  }

  @Override
  public boolean isSelected(ActionEvent e) {
    final String linkWithEditor = preferencesManager.getValue(LINK_WITH_EDITOR);
    return Boolean.parseBoolean(linkWithEditor);
  }

  @Override
  public void setSelected(ActionEvent e, boolean state) {
    preferencesManager.setValue(LINK_WITH_EDITOR, Boolean.toString(state));

    if (!state) {
      return;
    }

    final EditorPartPresenter activeEditor = editorAgentProvider.get().getActiveEditor();
    if (activeEditor == null) {
      return;
    }
    final EditorInput editorInput = activeEditor.getEditorInput();
    if (editorInput == null) {
      return;
    }
    eventBus.fireEvent(new RevealResourceEvent(editorInput.getFile().getLocation()));
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    activePart = event.getActivePart();
  }
}
