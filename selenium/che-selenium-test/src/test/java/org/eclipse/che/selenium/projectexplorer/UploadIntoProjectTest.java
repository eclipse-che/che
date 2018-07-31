/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import static java.lang.String.format;
import static java.nio.file.Paths.get;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.UPLOAD_FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.UPLOAD_FOLDER;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.upload.UploadDirectoryDialogPage;
import org.eclipse.che.selenium.pageobject.upload.UploadFileDialogPage;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class UploadIntoProjectTest {
  private static final String PROJECT_NAME = "TestProject";
  private static final URL PROJECT_SOURCES =
      UploadIntoProjectTest.class.getResource("/projects/default-spring-project");
  private static final URL FOLDER_SOURCES =
      UploadIntoProjectTest.class.getResource("/projects/git-pull-test");
  public static final String TEXT_TO_INSERT = NameGenerator.generate("", 10);

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private Events eventsPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private UploadFileDialogPage uploadFileDialogPage;
  @Inject private UploadDirectoryDialogPage uploadDirectoryDialogPage;
  @Inject private NotificationsPopupPanel notificationPopup;
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void setup() throws Exception {
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        get(PROJECT_SOURCES.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(testWorkspace);
    projectExplorer.waitVisibleItem(PROJECT_NAME);
  }

  @BeforeMethod
  public void selectProjectItem() {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
  }

  @Test
  public void shouldUploadFileWithDefaultOptions() throws URISyntaxException, IOException {
    // given
    final String uploadingFileName = "Aclass.java";
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFileName);
    final Path localPathToFileToUpload =
        get(PROJECT_SOURCES.getPath())
            .resolve("src/main/java/che/eclipse/sample")
            .resolve(uploadingFileName);

    // open upload file window
    openFormAndSelectUploadFile(localPathToFileToUpload);
    uploadFileDialogPage.clickOnUploadButton();

    // then
    uploadFileDialogPage.waitOnClose();
    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(
        format("File '%s' has uploaded successfully", uploadingFileName));
    projectExplorer.waitVisibleItem(format("%s/%s", PROJECT_NAME, uploadingFileName));

    // Check that uploading file doesn't overwrite existed one
    // when change the file
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    // when re-upload the file
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    openFormAndSelectUploadFile(localPathToFileToUpload);
    uploadFileDialogPage.clickOnUploadButton();

    // then there is changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(
        format("File '%s' has uploaded successfully", uploadingFileName));
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.waitTextIntoEditor(TEXT_TO_INSERT);
  }

  @Test
  public void shouldUploadFileWithOverwriting() throws IOException {
    // given
    final String uploadingFileName = "AppController.java";
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFileName);
    final Path localPathToFileToUpload =
        get(PROJECT_SOURCES.getPath())
            .resolve("src/main/java/org/eclipse/qa/examples")
            .resolve(uploadingFileName);

    // open upload file window
    openFormAndSelectUploadFile(localPathToFileToUpload);
    uploadFileDialogPage.clickOnUploadButton();

    // then
    uploadFileDialogPage.waitOnClose();
    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(
        format("File '%s' has uploaded successfully", uploadingFileName));
    projectExplorer.waitVisibleItem(format("%s/%s", PROJECT_NAME, uploadingFileName));

    // Check that uploading file overwrites existed one
    // when change the file
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    // when re-upload the file
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    openFormAndSelectUploadFile(localPathToFileToUpload);

    // then
    uploadFileDialogPage.selectOverwriteIfFileExistsCheckbox();
    uploadFileDialogPage.clickOnUploadButton();

    // then there are no changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(format("File '%s' is updated", uploadingFileName));
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.waitTextNotPresentIntoEditor(TEXT_TO_INSERT);
  }

  @Test
  public void shouldUploadDirectoryWithDefaultOptions() throws IOException {
    // given
    final String uploadingFileName = "index.jsp";
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFileName);
    final Path localPathToFolderToUpload =
        get(PROJECT_SOURCES.getPath()).resolve("src/main/webapp");

    // open upload directory window
    openFormAndUploadFolder(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);

    // Check that uploading directory doesn't overwrite existed one
    // when change the directory - when change the file
    projectExplorer.waitAndSelectItem(pathToUploadingFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    // when re-upload the directory
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    openFormAndUploadFolder(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then there is changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    projectExplorer.quickRevealToItemWithJavaScript(
        get(pathToUploadingFileInsideTheProject).getParent().toString());
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);

    try {
      editor.waitTextIntoEditor(TEXT_TO_INSERT);
    } catch (WebDriverException ex) {
      fail("Known issue https://github.com/eclipse/che/issues/10484", ex);
    }
  }

  @Test
  public void shouldUploadDirectoryWithOverwriting() throws IOException {
    // given
    final String uploadingHtmlFileName = "file.html";
    final String uploadingTextFileName = "readme-txt";
    final String uploadingTextFilePath = "plain-files/" + uploadingTextFileName;
    final String pathToUploadingHtmlFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingHtmlFileName);
    final String pathToUploadingTextFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingTextFilePath);

    final Path localPathToFolderToUpload = get(FOLDER_SOURCES.getPath());

    // open upload directory window
    openFormAndUploadFolder(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingTextFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingHtmlFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingTextFileInsideTheProject);

    // Check that uploading directory overwrites existed one
    // when change the directory - when change the files
    projectExplorer.waitAndSelectItem(pathToUploadingHtmlFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingHtmlFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    projectExplorer.waitAndSelectItem(pathToUploadingTextFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingTextFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    // when re-upload the directory with overwriting
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    openFormAndUploadFolder(localPathToFolderToUpload);

    // then
    uploadDirectoryDialogPage.selectOverwriteIfFileExistsCheckbox();
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then there are no changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingTextFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingTextFileInsideTheProject);

    try {
      editor.waitTextNotPresentIntoEditor(TEXT_TO_INSERT);
    } catch (WebDriverException ex) {
      fail("Known issue https://github.com/eclipse/che/issues/10437", ex);
    }

    projectExplorer.waitVisibleItem(pathToUploadingHtmlFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingHtmlFileInsideTheProject);
    editor.waitTextNotPresentIntoEditor(TEXT_TO_INSERT);
  }

  @Test
  public void shouldUploadDirectorySkippingRoot() throws IOException {
    // given
    final String uploadingFileName = "AppController.java";
    final String uploadingFilePath = "examples/" + uploadingFileName;
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFilePath);
    final Path localPathToFolderToUpload =
        get(PROJECT_SOURCES.getPath()).resolve("src/main/java/org/eclipse");

    // open upload directory window
    openFormAndUploadFolder(localPathToFolderToUpload);

    // then
    uploadDirectoryDialogPage.selectSkipRootFolderCheckbox();
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingFileInsideTheProject);

    try {
      projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
    } catch (WebDriverException ex) {
      fail("Known issue https://github.com/eclipse/che/issues/9430", ex);
    }
  }

  private void openFormAndSelectUploadFile(Path pathToUploadFile) throws IOException {
    menu.runCommand(PROJECT, UPLOAD_FILE);
    uploadFileDialogPage.waitOnOpen();
    uploadFileDialogPage.selectResourceToUpload(pathToUploadFile);
  }

  private void openFormAndUploadFolder(Path pathToUploadFolder) throws IOException {
    menu.runCommand(PROJECT, UPLOAD_FOLDER);
    uploadDirectoryDialogPage.waitOnOpen();
    uploadDirectoryDialogPage.selectResourceToUpload(pathToUploadFolder);
  }
}
