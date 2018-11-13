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
package org.eclipse.che.ide.part.editor.actions;

import static com.google.gwt.i18n.client.DateTimeFormat.getFormat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Date;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Performs closing selected editor. Note: the pane which contains this editor will be closed when
 * the pane doesn't contains editors anymore.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseAction extends EditorAbstractAction {

  @Inject
  public CloseAction(EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(locale.editorTabClose(), locale.editorTabCloseDescription(), null, editorAgent, eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    EditorTab editorTab = getEditorTab(e);
    if (editorTab == null) {
      return;
    }

    final VirtualFile editorFile = getEditorFile(e);
    final EditorPartPresenter openedEditor = editorAgent.getOpenedEditor(editorFile.getLocation());

    editorAgent.closeEditor(openedEditor);
    Log.info(
        this.getClass(),
        "actionPerformed() line 54, time: " + getFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
  }
}
