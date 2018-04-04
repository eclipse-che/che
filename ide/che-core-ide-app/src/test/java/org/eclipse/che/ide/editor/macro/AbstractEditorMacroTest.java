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
package org.eclipse.che.ide.editor.macro;

import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resource.Path;
import org.mockito.Mock;

/** @author Vlad Zhukovskyi */
public abstract class AbstractEditorMacroTest {

  public static final String FILE_NAME = "name.ext";
  public static final String FILE_NAME_WITHOUT_EXT = "name";
  public static final String FILE_PATH = "/project/name.ext";
  public static final String PROJECTS_ROOT = "/projects";
  public static final String PROJECT_NAME = "project-name";
  public static final String PROJECT_TYPE = "type";

  @Mock protected EditorAgent editorAgent;

  @Mock protected PromiseProvider promiseProvider;

  @Mock protected EditorPartPresenter activeEditor;

  @Mock protected File activeFile;

  @Mock protected EditorInput activeEditorInput;

  @Mock protected AppContext appContext;

  @Mock protected CoreLocalizationConstant localizationConstants;

  @Mock protected Project project;

  protected abstract AbstractEditorMacro getProvider();

  protected void initEditorWithTestFile() {
    when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
    when(activeEditor.getEditorInput()).thenReturn(activeEditorInput);
    when(activeEditorInput.getFile()).thenReturn(activeFile);
    when(activeFile.getName()).thenReturn(FILE_NAME);
    when(activeFile.getNameWithoutExtension()).thenReturn(FILE_NAME_WITHOUT_EXT);
    when(activeFile.getLocation()).thenReturn(Path.valueOf(FILE_PATH));
    when(appContext.getProjectsRoot()).thenReturn(Path.valueOf(PROJECTS_ROOT));
    when(activeFile.getRelatedProject()).thenReturn(Optional.of(project));
    when(project.getName()).thenReturn(PROJECT_NAME);
    when(project.getType()).thenReturn(PROJECT_TYPE);
  }
}
