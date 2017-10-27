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
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.IdeMainDockPanel;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class CreatePackagesAndFilesWithHelpIconCreateTest {

  private static final String PROJECT_NAME =
      CreatePackagesAndFilesWithHelpIconCreateTest.class.getSimpleName();
  private static final String NEW_PACKAGE_NAME = "newpackage";
  private static final String NEW_FOLDER_NAME = "newfolder";
  private static final String NEW_CLASS = "JavaClass";
  private static final String NEW_FILE_NAME = "file.txt";
  private static final String NEW_XML_FILE = "NewXml";
  private static final String NEW_LESS_FILE = "NewLess";
  private static final String NEW_CSS_FILE = "NewCSS";
  private static final String NEW_HTML_FILE = "NewHTML";
  private static final String NEW_JS_FILE = "NewJS";
  private static final String SOURCE_FOLDER = "src/main/java";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private IdeMainDockPanel ideMainDockPanel;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

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
  public void createWithHelpCreateIcon() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.selectVisibleItem("java");
    loader.waitOnClosed();

    // create new package
    ideMainDockPanel.clickCreateIcon();
    ideMainDockPanel.runCommandFromCreateIconList(IdeMainDockPanel.CreateMenuCommand.PACKAGE);
    askForValueDialog.createNotJavaFileByName(NEW_PACKAGE_NAME);
    projectExplorer.waitItemInVisibleArea(NEW_PACKAGE_NAME);

    // create new folder
    projectExplorer.waitItem(PROJECT_NAME + "/src/main");
    projectExplorer.selectItem(PROJECT_NAME + "/src/main");
    ideMainDockPanel.clickCreateIcon();
    ideMainDockPanel.runCommandFromCreateIconList(IdeMainDockPanel.CreateMenuCommand.FOLDER);
    askForValueDialog.createNotJavaFileByName(NEW_FOLDER_NAME);
    projectExplorer.waitItemInVisibleArea(NEW_FOLDER_NAME);

    /*create new java file*/
    projectExplorer.selectItem(PROJECT_NAME + "/" + SOURCE_FOLDER);
    ideMainDockPanel.clickCreateIcon();
    ideMainDockPanel.runCommandFromCreateIconList(IdeMainDockPanel.CreateMenuCommand.JAVA_CLASS);
    askForValueDialog.createJavaFileByNameAndType(NEW_CLASS, AskForValueDialog.JavaFiles.CLASS);
    projectExplorer.waitItemInVisibleArea(NEW_CLASS + ".java");
    editor.waitTabIsPresent(NEW_CLASS);
    loader.waitOnClosed();

    // create new files with different type
    createNewFile(NEW_FILE_NAME, IdeMainDockPanel.CreateMenuCommand.FILE, "");
    editor.waitTabIsPresent(NEW_FILE_NAME);
    loader.waitOnClosed();
    createNewFile(NEW_XML_FILE, IdeMainDockPanel.CreateMenuCommand.XML_FILE, ".xml");
    loader.waitOnClosed();
    createNewFile(NEW_LESS_FILE, IdeMainDockPanel.CreateMenuCommand.LESS_FILE, ".less");
    loader.waitOnClosed();
    createNewFile(NEW_CSS_FILE, IdeMainDockPanel.CreateMenuCommand.CSS_FILE, ".css");
    loader.waitOnClosed();
    createNewFile(NEW_HTML_FILE, IdeMainDockPanel.CreateMenuCommand.HTML_FILE, ".html");
    loader.waitOnClosed();
    createNewFile(NEW_JS_FILE, IdeMainDockPanel.CreateMenuCommand.JAVASCRIPT_FILE, ".js");
    loader.waitOnClosed();
  }

  public void createNewFile(String name, String type, String extFile) throws Exception {
    projectExplorer.selectItem(PROJECT_NAME + "/" + SOURCE_FOLDER);

    ideMainDockPanel.clickCreateIcon();
    ideMainDockPanel.runCommandFromCreateIconList(type);

    askForValueDialog.createNotJavaFileByName(name);
    projectExplorer.waitItemInVisibleArea(name + extFile);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    editor.waitTabIsPresent(name + extFile);
  }
}
