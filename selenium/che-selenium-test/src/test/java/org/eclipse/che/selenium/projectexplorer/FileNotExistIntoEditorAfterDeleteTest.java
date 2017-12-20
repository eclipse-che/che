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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class FileNotExistIntoEditorAfterDeleteTest {

  private static final String PROJECT_NAME = "FileNotExistIntoEditorAfterDelete";
  private static final String EXPECTED_TEXT_1 =
      "<%\n" + "   response.sendRedirect(\"spring/guess\");\n" + "%>";
  private static final String PATH_TO_JSP_FILE = PROJECT_NAME + "/src/main/webapp/index.jsp";
  private static final String PATH_TO_JAVA_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String EXPECTED_TEXT_2 =
      "package org.eclipse.qa.examples;\n"
          + "\n"
          + "import java.util.Random;\n"
          + "\n"
          + "import org.springframework.web.servlet.ModelAndView;\n"
          + "import org.springframework.web.servlet.mvc.Controller;\n"
          + "\n"
          + "import javax.servlet.http.HttpServletRequest;\n"
          + "import javax.servlet.http.HttpServletResponse;\n"
          + "\n"
          + "public class AppController implements Controller {\n"
          + "    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
          + "\n"
          + "    @Override\n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\";\n"
          + "        \n"
          + "        if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        } \n"
          + "        \n"
          + "        else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        }\n"
          + "\n"
          + "        ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view;\n"
          + "    }\n"
          + "}\n";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void fileNotExitInEditorAfterDeleting() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();

    // open .java file, get text from there and compare with expected text
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_TEXT_2);

    projectExplorer.selectItem(PATH_TO_JAVA_FILE);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);

    loader.waitOnClosed();
    askDialog.waitFormToOpen();

    loader.waitOnClosed();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();

    editor.waitWhileFileIsClosed("AppController.java");

    // open .jsp file, get text from there and compare with expected text
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByVisibleNameInExplorer("index.jsp");

    loader.waitOnClosed();
    editor.waitTextIntoEditor(EXPECTED_TEXT_1);

    projectExplorer.waitItem(PATH_TO_JSP_FILE);
    projectExplorer.openItemByPath(PATH_TO_JSP_FILE);
    projectExplorer.selectItem(PATH_TO_JSP_FILE);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();

    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();

    editor.waitWhileFileIsClosed("index");
  }
}
