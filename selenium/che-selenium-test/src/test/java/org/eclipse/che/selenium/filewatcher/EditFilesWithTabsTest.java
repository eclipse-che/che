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
package org.eclipse.che.selenium.filewatcher;

import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class EditFilesWithTabsTest {
  @Inject private TestWorkspace testWorkspace;

  @InjectPageObject(driverId = 1)
  private Ide ide1;

  @InjectPageObject(driverId = 1)
  private CodenvyEditor editor1;

  @InjectPageObject(driverId = 1)
  private ProjectExplorer projectExplorer1;

  @InjectPageObject(driverId = 2)
  private Ide ide2;

  @InjectPageObject(driverId = 2)
  private CodenvyEditor editor2;

  @InjectPageObject(driverId = 2)
  private ProjectExplorer projectExplorer2;

  @Inject private TestProjectServiceClient testProjectServiceClient;

  @InjectPageObject(driverId = 1)
  private Events eventsTab1;

  @InjectPageObject(driverId = 2)
  private Events eventsTab2;

  private String projectName = NameGenerator.generate("project", 6);

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/spring-project-for-file-watcher-tabs");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        projectName,
        ProjectTemplates.MAVEN_SPRING);

    ide1.open(testWorkspace);
    ide2.open(testWorkspace);

    prepareFiles();
  }

  @Test
  public void checkEditingFilesFromIde() throws Exception {
    String textForValidation = "//check test message for java file";
    String textForValidationTxt1 = "//check test message for file1.txt file";
    String expectedNotificationMess1 = "File 'AppController.java' is updated";
    String expectedNotificationMess2 = "File 'file1.txt' is updated";
    String nameReadmeFile = "README.md";
    String nameFiletxt1 = "file1.txt";

    editor1.setCursorToLine(1);
    editor1.typeTextIntoEditor(textForValidation);
    editor1.typeTextIntoEditor(Keys.ENTER.toString());

    eventsTab2.waitExpectedMessage(expectedNotificationMess1);
    editor2.waitTextIntoEditor(textForValidation);
    projectExplorer1.openItemByPath(projectName + "/" + nameReadmeFile);
    projectExplorer1.openItemByPath(projectName + "/" + nameFiletxt1);
    projectExplorer2.openItemByPath(projectName + "/" + nameReadmeFile);
    projectExplorer2.openItemByPath(projectName + "/" + nameFiletxt1);
    editor2.typeTextIntoEditor(textForValidationTxt1);

    eventsTab1.waitExpectedMessage(expectedNotificationMess2);
    eventsTab1.clearAllMessages();

    editor1.closeFileByNameWithSaving(nameFiletxt1);
    editor2.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor2.typeTextIntoEditor(Keys.BACK_SPACE.toString());

    checkThatNotificationIsNotAppear(8, 500, expectedNotificationMess1);
  }

  /** Opens same file in two browsers */
  private void prepareFiles() {
    expandFoldersToClass(projectExplorer1, editor1);
    expandFoldersToClass(projectExplorer2, editor2);
    eventsTab1.clickEventLogBtn();
    eventsTab2.clickEventLogBtn();
  }

  /** Expands project for a defined browser instance ('user') */
  private void expandFoldersToClass(ProjectExplorer projectExplorer, CodenvyEditor editor) {
    projectExplorer.waitItem(projectName);
    projectExplorer.quickExpandWithJavaScript();

    String path_for_expand = projectName + "/src/main/java/org.eclipse.qa.examples";
    projectExplorer.openItemByPath(path_for_expand.replace(".", "/") + "/AppController.java");

    editor.waitActive();
  }

  /** Performs checking that notifications do not appear for inactive tabs */
  private void checkThatNotificationIsNotAppear(
      int amountOfRequests, int delayBetweenRequsts, String expMess) {
    for (int i = 0; i < amountOfRequests; i++) {
      sleepQuietly(delayBetweenRequsts, TimeUnit.MILLISECONDS);
      editor1.waitTextNotPresentIntoEditor(expMess);
    }
  }
}
