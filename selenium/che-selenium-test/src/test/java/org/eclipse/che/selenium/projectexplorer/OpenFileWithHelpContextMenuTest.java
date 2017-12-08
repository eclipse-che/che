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
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Alexander Andrienko
 * @author Andrey Chizhikov
 */
public class OpenFileWithHelpContextMenuTest {

  private static final String PROJECT_NAME = OpenFileWithHelpContextMenuTest.class.getSimpleName();
  private static final String NAME_JSP = "index.jsp";
  private static final String NAME_LESS = "LessFile.less";
  private static final String NAME_CSS = "cssFile.css";
  private static final String NAME_XML = "web.xml";
  private static final String NAME_HTML = "htmlFile.html";
  private static final String NAME_JAVA_CLASS = "AppController";
  private static final String NAME_FILE = "another";
  private static final String NAME_SQL = "sqlFile.sql";
  private static final String PATH_TO_JSP = PROJECT_NAME + "/src/main/webapp/index.jsp";
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_LESS =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/LessFile.less";
  private static final String PATH_TO_CSS = PROJECT_NAME + "/src/main/webapp/WEB-INF/cssFile.css";
  private static final String PATH_TO_XML = PROJECT_NAME + "/src/main/webapp/WEB-INF/web.xml";
  private static final String PATH_TO_HTML =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/htmlFile.html";
  private static final String PATH_TO_JAVA_CLASS =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_TO_SIMPLE_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/another";
  private static final String PATH_TO_SQL =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/sqlFile.sql";
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String JSP_TEXT =
      "<%\n" + "   response.sendRedirect(\"spring/guess\");\n" + "\n" + "%>";
  private static final String CSS_TEST =
      "@CHARSET \"UTF-8\";\n"
          + "h1 {\n"
          + "    font-size:180%;\n"
          + "}\n"
          + "\n"
          + "h2 {\n"
          + "    font-size:190%;\n"
          + "}\n"
          + "\n"
          + "p {\n"
          + "    font-size:110%;\n"
          + "}";

  private static final String LESS_TEXT = "@CHARSET \"UTF-8\"\n" + ";";
  private static final String XML_TEXT =
      "<!DOCTYPE web-app PUBLIC\n"
          + " \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\n"
          + " \"http://java.sun.com/dtd/web-app_2_3.dtd\" >\n"
          + "\n"
          + "<web-app>\n"
          + "   <display-name>Spring Web Application</display-name>\n"
          + "   <servlet>\n"
          + "      <servlet-name>spring</servlet-name>\n"
          + "      <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>\n"
          + "      <load-on-startup>1</load-on-startup>\n"
          + "   </servlet>\n"
          + "   <servlet-mapping>\n"
          + "      <servlet-name>spring</servlet-name>\n"
          + "      <url-pattern>/spring/*</url-pattern>\n"
          + "   </servlet-mapping>\n"
          + "</web-app>\n";
  private static final String HTML_TEXT =
      "<!DOCTYPE html>\n"
          + "<html lang=\"en\">\n"
          + "<head>\n"
          + "    <meta charset=\"UTF-8\">\n"
          + "    <title>Title</title>\n"
          + "</head>\n"
          + "<body>\n"
          + "\n"
          + "</body>\n"
          + "</html>";
  private static final String APP_CONTROLLER_TEXT =
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
  private static final String ANOTHER_FILE_TEXT =
      "testExample1\n"
          + "testExample2\n"
          + "testExample3\n"
          + "testExample4\n"
          + "testExample5\n"
          + "testExample6\n"
          + "testExample7\n"
          + "testExample8\n"
          + "testExample9\n"
          + "testExample10\n"
          + "testExample11";
  private static final String SQL_TEXT =
      "Select * From Car Where Car.id > 100 and Car.speed < 180\n"
          + "Select * From Car Where Car.id > 100 and Car.speed < 190\n"
          + "Select * From Car Where Car.id > 100 and Car.speed < 210\n"
          + "\n"
          + "\n"
          + "\n"
          + "Select * From Car Where Car.id > 200 and Car.speed < 300";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void openFileTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_FOR_EXPAND + "/AppController.java");
    editor.waitActive();
    editor.closeFileByNameWithSaving("AppController");
    projectExplorer.openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    editor.waitActive();
    editor.closeFileByNameWithSaving("index.jsp");

    openFileFromContextMenu(PATH_TO_JAVA_CLASS, NAME_JAVA_CLASS, APP_CONTROLLER_TEXT);

    openFileFromContextMenu(PATH_TO_JSP, NAME_JSP, JSP_TEXT);

    openFileFromContextMenu(PATH_TO_CSS, NAME_CSS, CSS_TEST);

    openFileFromContextMenu(PATH_TO_XML, NAME_XML, XML_TEXT);

    openFileFromContextMenu(PATH_TO_LESS, NAME_LESS, LESS_TEXT);

    openFileFromContextMenu(PATH_TO_HTML, NAME_HTML, HTML_TEXT);

    openFileFromContextMenu(PATH_TO_SIMPLE_FILE, NAME_FILE, ANOTHER_FILE_TEXT);

    openFileFromContextMenu(PATH_TO_SQL, NAME_SQL, SQL_TEXT);
  }

  private void openFileFromContextMenu(String pathToFile, String fileName, String expectedContent) {

    loader.waitOnClosed();
    projectExplorer.waitItem(pathToFile);
    projectExplorer.openContextMenuByPathSelectedItem(pathToFile);
    projectExplorer.clickOnNewContextMenuItem(TestProjectExplorerContextMenuConstants.EDIT);
    loader.waitOnClosed();
    editor.waitTabIsPresent(fileName);
    loader.waitOnClosed();
    editor.waitActive();
    editor.waitTextIntoEditor(expectedContent);
  }
}
