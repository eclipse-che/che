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
package org.eclipse.che.ide.ext.java.client.action;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Base action for Java editor related action.
 *
 * @author Evgen Vidolob
 */
public abstract class JavaEditorAction extends ProjectAction {

  private final FileTypeRegistry fileTypeRegistry;
  protected EditorAgent editorAgent;

  public JavaEditorAction(
      String text,
      String description,
      SVGResource svgIcon,
      EditorAgent editorAgent,
      FileTypeRegistry fileTypeRegistry) {
    super(text, description, svgIcon);
    this.editorAgent = editorAgent;
    this.fileTypeRegistry = fileTypeRegistry;
  }

  public JavaEditorAction(
      String text, String description, EditorAgent editorAgent, FileTypeRegistry fileTypeRegistry) {
    this(text, description, null, editorAgent, fileTypeRegistry);
  }

  @Override
  protected void updateProjectAction(ActionEvent e) {
    if (editorAgent.getActiveEditor() != null) {
      EditorInput input = editorAgent.getActiveEditor().getEditorInput();
      VirtualFile file = input.getFile();
      final String fileExtension = fileTypeRegistry.getFileTypeByFile(file).getExtension();
      if ("java".equals(fileExtension) || "class".equals(fileExtension)) {
        e.getPresentation().setEnabledAndVisible(true);
        return;
      }
    }
    e.getPresentation().setEnabledAndVisible(false);
  }
}
