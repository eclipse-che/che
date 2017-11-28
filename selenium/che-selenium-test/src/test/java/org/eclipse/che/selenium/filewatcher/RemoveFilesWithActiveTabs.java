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
package org.eclipse.che.selenium.filewatcher;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.DELETE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.EDIT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.IdeMainDockPanel;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * //
 *
 * @author Musienko Maxim
 */
public class RemoveFilesWithActiveTabs {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);
  @Inject private TestWorkspace ws;

  @InjectPageObject(driverId = 1)
  private Ide ide1;

  @InjectPageObject(driverId = 1)
  private ProjectExplorer projectExplorer1;

  @InjectPageObject(driverId = 1)
  private Events events1;

  @InjectPageObject(driverId = 1)
  private NotificationsPopupPanel notifications1;

  @InjectPageObject(driverId = 1)
  private Menu menu1;

  @InjectPageObject(driverId = 1)
  private Loader loader1;

  @InjectPageObject(driverId = 1)
  private CodenvyEditor editor1;

  @InjectPageObject(driverId = 1)
  private IdeMainDockPanel ideMainDockPanel1;

  @InjectPageObject(driverId = 1)
  private AskDialog askDialog1;

  @InjectPageObject(driverId = 2)
  private Ide ide2;

  @InjectPageObject(driverId = 2)
  private CodenvyEditor editor2;

  @InjectPageObject(driverId = 2)
  private Events events2;

  @InjectPageObject(driverId = 2)
  private ProjectExplorer projectExplorer2;

  @InjectPageObject(driverId = 2)
  private Refactor refactorPanel2;

  @InjectPageObject(driverId = 2)
  private Menu menu2;

  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/spring-project-for-file-watcher-tabs");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);

    ide1.open(ws);
    ide2.open(ws);

    prepareFiles();
  }

  @Test
  public void checkDeletionWithSingleOpenedTabFromIde() throws Exception {
    String deletedClass = "AppController.java";
    String expectedMessage = "File '" + deletedClass + "' is removed";
    projectExplorer1.selectVisibleItem(deletedClass);
    menu1.runCommand(EDIT, DELETE);
    projectExplorer2.openItemByVisibleNameInExplorer(deletedClass);
    editor2.waitActiveEditor();

    confirmDeletion();

    waitExpectedMessageInEventPanel(events2, expectedMessage);
    editor2.waitTabIsNotPresent(deletedClass.replace(".java", ""));
    projectExplorer2.waitItemIsNotPresentVisibleArea(deletedClass);
  }

  @Test(priority = 1)
  public void checkDeletionWithMultiOpenedTabFromIde() throws Exception {
    String nameReadmeFile = "README.md";
    String nameFiletxt1 = "file1.txt";
    String expectedMessage1 = "File '" + nameReadmeFile + "' is removed";
    String expectedMessage2 = "File '" + nameFiletxt1 + "' is removed";
    projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameReadmeFile);
    projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameFiletxt1);
    projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameReadmeFile);
    projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameFiletxt1);
    projectExplorer1.selectItem(PROJECT_NAME + "/" + nameReadmeFile);
    projectExplorer1.selectMultiFilesByCtrlKeys(PROJECT_NAME + "/" + nameFiletxt1);
    menu1.runCommand(EDIT, DELETE);

    confirmDeletion();

    waitExpectedMessageInEventPanel(events2, expectedMessage1);
    waitExpectedMessageInEventPanel(events2, expectedMessage2);
    projectExplorer1.waitItemIsNotPresentVisibleArea(nameReadmeFile);
    projectExplorer1.waitItemIsNotPresentVisibleArea(nameFiletxt1);
    projectExplorer2.waitItemIsNotPresentVisibleArea(nameReadmeFile);
    projectExplorer2.waitItemIsNotPresentVisibleArea(nameFiletxt1);
  }

  @Test(priority = 2)
  public void checkRemovingWithoutIde() throws Exception {
    String nameFiletxt2 = "file2.txt";
    String nameFiletxt3 = "file3.txt";
    String expectedMessage1 = "File '" + nameFiletxt2 + "' is removed";
    String expectedMessage2 = "File '" + nameFiletxt3 + "' is removed";
    projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameFiletxt2);
    editor1.waitTabIsPresent(nameFiletxt2);

    testProjectServiceClient.deleteResource(ws.getId(), PROJECT_NAME + "/" + nameFiletxt2);

    waitExpectedMessageInEventPanel(events1, expectedMessage1);
    editor1.waitTabIsNotPresent(nameFiletxt2);
    projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameFiletxt3);
    editor2.waitTabIsPresent(nameFiletxt3);

    testProjectServiceClient.deleteResource(ws.getId(), PROJECT_NAME + "/" + nameFiletxt3);

    editor2.waitTabIsNotPresent(nameFiletxt2);
    waitExpectedMessageInEventPanel(events2, expectedMessage2);
  }

  /** Handles the IDE deletion dialog */
  private void confirmDeletion() {
    askDialog1.waitFormToOpen();
    askDialog1.clickOkBtn();
    loader1.waitOnClosed();
    askDialog1.waitFormToClose();
  }

  /** Expands tree of project explorer in two browsers */
  private void prepareFiles() {
    projectExplorer1.waitItem(PROJECT_NAME);
    projectExplorer1.quickExpandWithJavaScript();

    projectExplorer2.waitItem(PROJECT_NAME);
    projectExplorer2.quickExpandWithJavaScript();

    events1.clickEventLogBtn();
    events2.clickEventLogBtn();
  }

  private void waitExpectedMessageInEventPanel(Events event, String expectedMessage) {
    try {
      event.waitExpectedMessage(expectedMessage, LOAD_PAGE_TIMEOUT_SEC);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7339");
    }
  }
}
