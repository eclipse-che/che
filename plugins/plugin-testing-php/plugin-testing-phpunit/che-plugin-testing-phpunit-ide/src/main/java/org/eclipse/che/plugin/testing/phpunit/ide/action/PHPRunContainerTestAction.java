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

import static java.util.Arrays.asList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resources.tree.ContainerNode;
import org.eclipse.che.plugin.testing.ide.TestActionRunner;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestResources;

/**
 * "Run Container" PHPUnit test action.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPRunContainerTestAction extends AbstractPerspectiveAction {

  private final TestActionRunner runner;
  private DtoFactory dtoFactory;
  private final AppContext appContext;
  private final SelectionAgent selectionAgent;

  @Inject
  public PHPRunContainerTestAction(
      TestActionRunner runner,
      DtoFactory dtoFactory,
      PHPUnitTestResources resources,
      AppContext appContext,
      SelectionAgent selectionAgent,
      PHPUnitTestLocalizationConstant localization) {
    super(
        asList(PROJECT_PERSPECTIVE_ID),
        localization.actionRunContainerTitle(),
        localization.actionRunScriptDescription(),
        null,
        resources.testIcon());
    this.runner = runner;
    this.dtoFactory = dtoFactory;
    this.appContext = appContext;
    this.selectionAgent = selectionAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Selection<?> selection = selectionAgent.getSelection();
    final Object possibleNode = selection.getHeadElement();
    if (possibleNode instanceof ContainerNode) {
      Container container = ((ContainerNode) possibleNode).getData();
      final Project project = appContext.getRootProject();

      TestExecutionContext executionContext = dtoFactory.createDto(TestExecutionContext.class);
      executionContext.setFrameworkName("PHPUnit");
      executionContext.setFilePath(container.getLocation().toString());
      executionContext.setProjectPath(project.getPath());
      executionContext.setDebugModeEnable(false);

      runner.run(executionContext);
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
    if (possibleNode instanceof ContainerNode) {
      String projectType = ((ContainerNode) possibleNode).getData().getProject().getType();
      if ("php".equalsIgnoreCase(projectType) || "composer".equalsIgnoreCase(projectType)) {
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(true);
        return;
      }
    }
    e.getPresentation().setVisible(false);
    e.getPresentation().setEnabled(false);
  }
}
