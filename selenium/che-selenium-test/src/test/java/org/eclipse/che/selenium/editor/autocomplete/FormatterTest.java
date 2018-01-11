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
package org.eclipse.che.selenium.editor.autocomplete;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Zaryana Dombrovskaya */
public class FormatterTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(FormatterTest.class.getSimpleName(), 4);
  private static final String FORMATTED_TEXT =
      "/*\n"
          + " * Copyright (c) 2012-2017 Red Hat, Inc.\n"
          + " * All rights reserved. This program and the accompanying materials\n"
          + " * are made available under the terms of the Eclipse Public License v1.0\n"
          + " * which accompanies this distribution, and is available at\n"
          + " * http://www.eclipse.org/legal/epl-v10.html\n"
          + " *\n"
          + " * Contributors:\n"
          + " *   Red Hat, Inc. - initial API and implementation\n"
          + " */\n"
          + "package org.eclipse.qa.examples;\n"
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
          + "\n"
          + "        if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        }\n"
          + "\n"
          + "        else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        }\n"
          + "\n"
          + "        ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view;\n"
          + "    }\n"
          + "}\n";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/not-formated-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void formatterTest() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.selectVisibleItem("AppController.java");
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");

    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FORMAT);

    editor.waitActive();
    editor.waitTextIntoEditor(FORMATTED_TEXT);
    loader.waitOnClosed();
  }
}
