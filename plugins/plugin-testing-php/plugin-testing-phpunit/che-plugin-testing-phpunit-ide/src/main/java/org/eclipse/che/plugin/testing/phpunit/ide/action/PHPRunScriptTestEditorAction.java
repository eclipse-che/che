/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.testing.phpunit.ide.action;

import com.google.inject.Inject;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.testing.ide.TestActionRunner;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestResources;

/**
 * "Run Script" PHPUnit test editor action.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPRunScriptTestEditorAction extends ProjectAction {

  private final TestActionRunner runner;
  private DtoFactory dtoFactory;
  private final AppContext appContext;
  private final EditorAgent editorAgent;
  private final FileTypeRegistry fileTypeRegistry;

  @Inject
  public PHPRunScriptTestEditorAction(
      TestActionRunner runner,
      DtoFactory dtoFactory,
      EditorAgent editorAgent,
      FileTypeRegistry fileTypeRegistry,
      PHPUnitTestResources resources,
      AppContext appContext,
      PHPUnitTestLocalizationConstant localization) {
    super(
        localization.actionRunScriptTitle(),
        localization.actionRunScriptDescription(),
        resources.testIcon());
    this.runner = runner;
    this.dtoFactory = dtoFactory;
    this.appContext = appContext;
    this.editorAgent = editorAgent;
    this.fileTypeRegistry = fileTypeRegistry;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Project project = appContext.getRootProject();
    EditorPartPresenter editorPart = editorAgent.getActiveEditor();
    final VirtualFile file = editorPart.getEditorInput().getFile();

    TestExecutionContext executionContext = dtoFactory.createDto(TestExecutionContext.class);
    executionContext.setFrameworkName("PHPUnit");
    executionContext.setFilePath(file.getLocation().toString());
    executionContext.setProjectPath(project.getPath());
    executionContext.setDebugModeEnable(false);

    runner.run(executionContext);
  }

  @Override
  protected void updateProjectAction(ActionEvent e) {
    if (editorAgent.getActiveEditor() != null) {
      EditorInput input = editorAgent.getActiveEditor().getEditorInput();
      VirtualFile file = input.getFile();
      final String fileExtension = fileTypeRegistry.getFileTypeByFile(file).getExtension();
      if ("php".equals(fileExtension) || "phtml".equals(fileExtension)) {
        e.getPresentation().setEnabledAndVisible(true);
        return;
      }
    }
    e.getPresentation().setEnabledAndVisible(false);
  }
}
