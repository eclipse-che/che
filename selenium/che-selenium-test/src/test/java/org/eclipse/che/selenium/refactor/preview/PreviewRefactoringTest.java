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
package org.eclipse.che.selenium.refactor.preview;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class PreviewRefactoringTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(PreviewRefactoringTest.class.getSimpleName(), 3);

  private static final String EXPECTED_TEXT_BEFORE_CHANGE =
      "import javax.servlet.http.HttpServletResponse;\n"
          + " public class AppController implements Controller {\n"
          + "    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
          + "     @Override \n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String a = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\"; \n"
          + "                 if (a != null && a.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        }  \n"
          + "                 else if (a != null) { \n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        } \n"
          + "         ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);";

  private static final String EXPECTED_TEXT_BEFORE_FIRST_SELECT_CHANGE =
      "    @Override \n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\"; \n"
          + "                 if (a != null && numGuessByUser.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        }  \n"
          + "                 else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        } \n"
          + "         ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view; \n"
          + "    } \n"
          + "} ";

  private static final String EXPECTED_TEXT_BEFORE_SECOND_SELECT_CHANGE =
      "    @Override \n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\"; \n"
          + "                 if (numGuessByUser != null && \n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        }  \n"
          + "                 else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        } \n"
          + "         ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view; \n"
          + "    } \n"
          + "}";

  private static final String EXPECTED_TEXT_IN_LEFT_EDITOR =
      "import javax.servlet.http.HttpServletResponse;\n"
          + " public class AppController implements Controller {\n"
          + "    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
          + "     @Override \n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\"; \n"
          + "                 if (numGuessByUser != null && numGuessByUser\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        }  \n"
          + "                 else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";";
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer explorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactorPanel;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkPreviewRefreshTest() {
    explorer.waitProjectExplorer();
    loader.waitOnClosed();
    explorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    explorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(27, 17);
    editor.launchRefactorForm();
    refactorPanel.waitRenameParametersFormIsOpen();
    refactorPanel.typeAndWaitNewName("a");
    refactorPanel.clickPreviewButtonRefactorForm();
    refactorPanel.clickOnItemByNameAndPosition("AppController.java", 0);
    loader.waitOnClosed();
    refactorPanel.checkTextFromLeftEditor(EXPECTED_TEXT_IN_LEFT_EDITOR);
    refactorPanel.checkTextFromRightEditor(EXPECTED_TEXT_BEFORE_CHANGE);
    Assert.assertEquals(refactorPanel.getQuantityLeasedLineInLeftEditor(), 3);
    Assert.assertEquals(refactorPanel.getQuantityLeasedLineInRightEditor(), 3);
    refactorPanel.clickOnExpandItemByNameAndPosition("AppController", 0);
    refactorPanel.clickOnItemByNameAndPosition("Textual change", 1);
    loader.waitOnClosed();
    refactorPanel.checkTextFromRightEditor(EXPECTED_TEXT_BEFORE_FIRST_SELECT_CHANGE);
    refactorPanel.clickOnItemByNameAndPosition("Textual change", 2);
    loader.waitOnClosed();
    refactorPanel.checkTextFromRightEditor(EXPECTED_TEXT_BEFORE_SECOND_SELECT_CHANGE);
    Assert.assertEquals(refactorPanel.getQuantityLeasedLineInLeftEditor(), 1);
    Assert.assertEquals(refactorPanel.getQuantityLeasedLineInRightEditor(), 1);
  }
}
