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

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.plugin.testing.ide.TestActionRunner;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestResources;

/**
 * "Run Script" PHPUnit test action.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPRunScriptTestAction extends AbstractPerspectiveAction {

  private final TestActionRunner runner;
  private final AppContext appContext;
  private final SelectionAgent selectionAgent;

  @Inject
  public PHPRunScriptTestAction(
      TestActionRunner runner,
      PHPUnitTestResources resources,
      AppContext appContext,
      SelectionAgent selectionAgent,
      PHPUnitTestLocalizationConstant localization) {
    super(
        Arrays.asList(PROJECT_PERSPECTIVE_ID),
        localization.actionRunScriptTitle(),
        localization.actionRunScriptDescription(),
        null,
        resources.testIcon());
    this.runner = runner;
    this.appContext = appContext;
    this.selectionAgent = selectionAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Selection<?> selection = selectionAgent.getSelection();
    final Object possibleNode = selection.getHeadElement();
    if (possibleNode instanceof FileNode) {
      VirtualFile file = ((FileNode) possibleNode).getData();
      final Project project = appContext.getRootProject();
      Map<String, String> parameters = new HashMap<>();
      parameters.put("testTarget", file.getLocation().toString());
      runner.run("PHPUnit", project.getPath(), parameters);
    }
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    if ((appContext.getRootProject() == null)) {
      e.getPresentation().setVisible(true);
      e.getPresentation().setEnabled(false);
      return;
    }
    final Selection<?> selection = selectionAgent.getSelection();
    if (selection == null || selection.isEmpty()) {
      e.getPresentation().setEnabled(false);
      return;
    }
    if (selection.isMultiSelection()) {
      e.getPresentation().setEnabled(false);
      return;
    }
    final Object possibleNode = selection.getHeadElement();
    boolean enable = false;
    if (possibleNode instanceof FileNode) {
      FileNode fileNode = (FileNode) possibleNode;
      File data = fileNode.getData();
      String extension = data.getExtension();
      if ("php".equals(extension) || "phtml".equals(extension)) {
        enable = true;
      }
    }

    e.getPresentation().setEnabled(enable);
    e.getPresentation().setVisible(enable);
  }
}
